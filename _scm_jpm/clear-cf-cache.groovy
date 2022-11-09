#!/bin/groovy
def run(props, jobManifest) {
  debug.info()
  def jobVars = jobManifest.getJobVars()
  def dockerImage = util.getValueHelper('dockerImage', 'jnj.artifactrepo.jnj.com/jpm/awscli', jobVars?.s3, jobVars?.deploy)
  ensure.insideDockerContainer(dockerImage, '') {
    try {
      def credentialsId = util.getValueHelper('credentialsId', null, jobVars?.s3, jobVars?.deploy)
      if (!credentialsId) {
        throw new RuntimeException("CloudFront requires credentialsId to be defined")
      }
      def cloudFrontDistId = util.getValueHelper('cloudFrontDistId', null, jobVars?.s3, jobVars?.deploy)
      if (!cloudFrontDistId) {
        throw new RuntimeException("CloudFront requires distribution-id to be defined")
      }

      def awsRegion = util.getValueHelper('awsRegion', 'us-east-1', jobVars?.s3, jobVars?.deploy)
      def cfCmd = "aws cloudfront create-invalidation --distribution-id ${cloudFrontDistId} --paths '/*'"
      pPrint.info("Clear CloudFront Cache: \n\tfrom=${cloudFrontDistId} \n\tcmd=${cfCmd}")

      def credentialType = util.getCredentialType(credentialsId)
      def creds = []
      if (credentialType == 'com.cloudbees.jenkins.plugins.awscredentials.AWSCredentialsImpl') {
        creds << [$class:          'AmazonWebServicesCredentialsBinding',
                          credentialsId:     credentialsId,
                          accessKeyVariable: 'AWS_ACCESS_KEY_ID',
                          secretKeyVariable: 'AWS_SECRET_ACCESS_KEY']
      } else {
        creds << usernamePassword(
                          credentialsId:     credentialsId,
                          usernameVariable: 'AWS_ACCESS_KEY_ID',
                          passwordVariable: 'AWS_SECRET_ACCESS_KEY')
      }
      withEnv(["AWS_DEFAULT_REGION=${awsRegion}"]) {
        withCredentials(creds) {
          sh "pwd"
          sh "${cfCmd}"
        }
      }
    }
    catch (Exception e) {
      throw new Exception("Error executing Cloudfront Cache Clear", e)
    }
  }
}

return this;