#!/bin/bash

export PROJECT="tw-rd-de-max"
export REGION="asia-east1"
export BUCKET="dataflow-max-example"
export DATASET="dataflow_example"
export EVEN_TABLE="even_table"
export ODD_TABLE="odd_table"
export TEMPLATE_IMAGE="gcr.io/$PROJECT/samples/dataflow-sample:latest"
export TEMPLATE_PATH="gs://$BUCKET/samples/metadata.json"
export BOOTSTRAP_SERVER="*.*.*.*:*"
export INPUT_TOPIC="quickstart-events"

echo "====GCP ENV===="
echo "Project name: $PROJECT"
echo "Region: $REGION"
echo "Output even table: $PROJECT:$DATASET.$EVEN_TABLE"
echo "Output odd table: $PROJECT:$DATASET.$ODD_TABLE"
echo "Template path on GCS: $TEMPLATE_PATH"

echo "====Kafka ENV===="
echo "Kafka server: $BOOTSTRAP_SERVER"
echo "Kafka topic: $INPUT_TOPIC"

gcloud config set project "$PROJECT" && gcloud config list | grep -i 'project'

mvn clean package

gcloud dataflow flex-template build "$TEMPLATE_PATH" \
      --image-gcr-path "$TEMPLATE_IMAGE" \
      --sdk-language "JAVA" \
      --flex-template-base-image JAVA11 \
      --metadata-file "metadata.json" \
      --jar "target/dataflow-1.0.0.jar" \
      --env FLEX_TEMPLATE_JAVA_MAIN_CLASS="org.apache.beam.samples.main.KafkaToBigQuery"

gcloud dataflow flex-template run "kafka-to-bigquery-`date +%Y%m%d-%H%M%S`" \
    --template-file-gcs-location "$TEMPLATE_PATH" \
    --parameters inputTopic="$INPUT_TOPIC" \
    --parameters outputEvenTable="$PROJECT:$DATASET.$EVEN_TABLE" \
    --parameters outputOddTable="$PROJECT:$DATASET.$ODD_TABLE" \
    --parameters bootstrapServer="$BOOTSTRAP_SERVER" \
    --region "$REGION"
