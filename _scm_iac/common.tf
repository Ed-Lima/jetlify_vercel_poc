provider "aws" {
  region   = "us-east-1"
  ignore_tags {
    key_prefixes = ["vpcx-"]
  }
}