#!/usr/bin/env bash
set -euo pipefail

REGION=us-east-1
ENDPOINT=http://localhost:4566

awslocal sns create-topic --name order-events --region "$REGION"

awslocal sqs create-queue \
  --queue-name order-processing-dlq \
  --region "$REGION"

DLQ_ARN=$(awslocal sqs get-queue-attributes \
  --queue-url "$ENDPOINT/000000000000/order-processing-dlq" \
  --attribute-names QueueArn \
  --query 'Attributes.QueueArn' --output text)

awslocal sqs create-queue \
  --queue-name order-processing \
  --attributes "{
    \"RedrivePolicy\": \"{\\\"deadLetterTargetArn\\\":\\\"$DLQ_ARN\\\",\\\"maxReceiveCount\\\":\\\"3\\\"}\",
    \"VisibilityTimeout\": \"30\"
  }" \
  --region "$REGION"

TOPIC_ARN=$(awslocal sns list-topics --query "Topics[?contains(TopicArn, 'order-events')].TopicArn" --output text)
QUEUE_ARN=$(awslocal sqs get-queue-attributes \
  --queue-url "$ENDPOINT/000000000000/order-processing" \
  --attribute-names QueueArn \
  --query 'Attributes.QueueArn' --output text)

awslocal sns subscribe \
  --topic-arn "$TOPIC_ARN" \
  --protocol sqs \
  --notification-endpoint "$QUEUE_ARN" \
  --attributes RawMessageDelivery=true

awslocal sqs set-queue-attributes \
  --queue-url "$ENDPOINT/000000000000/order-processing" \
  --attributes "Policy={\"Version\":\"2012-10-17\",\"Statement\":[{\"Effect\":\"Allow\",\"Principal\":\"*\",\"Action\":\"sqs:SendMessage\",\"Resource\":\"$QUEUE_ARN\",\"Condition\":{\"ArnEquals\":{\"aws:SourceArn\":\"$TOPIC_ARN\"}}}]}"

echo "LocalStack SNS/SQS ready (topic + main queue + DLQ)."
