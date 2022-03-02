gcloud dataflow jobs list \
  --filter 'STATE=Failed' \
  --format 'value(JOB_ID)' \
  --region "$REGION" \
  | xargs gcloud dataflow jobs cancel --region "$REGION"

gsutil rm "$TEMPLATE_PATH"

gcloud container images delete "$TEMPLATE_IMAGE" --force-delete-tags