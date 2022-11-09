output "base_url" {
  value = aws_api_gateway_deployment.agw.invoke_url
}

output "execution_arn" {
  value = aws_api_gateway_rest_api.rest.execution_arn
}