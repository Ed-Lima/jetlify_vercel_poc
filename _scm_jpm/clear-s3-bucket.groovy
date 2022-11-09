#!/bin/groovy
def run(props, jobManifest) {
  debug.info()
  def jobVars = jobManifest.getJobVars()
  def dockerImage = util.getValueHelper('dockerImage', 'jnj.artifactrepo.jnj.com/jpm/awscli', jobVars?.s3, jobVars?.deploy)
  ensure.insideDockerContainer(dockerImage, '') {
    try {
      def credentialsId = util.getValueHelper('credentialsId', null, props, jobVars?.s3)
      def bucket = util.getValueHelper('s3Bucket', null, props, jobVars?.s3)
      def bucketPath = util.getValueHelper('s3BucketPath', null, props, jobVars?.s3)
      def destPath = util.getValueHelper('dstPath', null, props, jobVars?.s3)
      def awsRegion = util.getValueHelper('awsRegion', 'us-east-1', props, jobVars?.s3)

      if (!credentialsId) {
        throw new RuntimeException("s3 requires credentialsId to be defined")
      }
      if (!bucket) {
        throw new RuntimeException("s3 requires bucket to be defined")
      }
      bucket = bucket.replaceAll('/$', "").replaceAll('^/', "")
      // Setup s3 bucket target folder.
      def dest = "s3://${bucket}/"
      if (bucketPath) {
        dest += bucketPath.replaceAll('/$', "").replaceAll('^/', "")
      }
      if (destPath) {
        dest += "/" + destPath.replaceAll('/$', "").replaceAll('^/', "")
      }
      // restore the trailing / if it's meaningful for the upload
      if (!destPath && bucketPath && bucketPath.endsWith("/")) {
        dest += "/"
      } else if (destPath && destPath.endsWith("/")) {
        dest += "/"
      }

      def s3Cmd = "aws s3 rm ${dest} --recursive"
      pPrint.info("Clear Bucket Content: \n\tfrom=${dest} \n\tcmd=${s3Cmd}")

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
          sh "${s3Cmd}"
        }
      }
    }
    catch (Exception e) {
      throw new Exception("Error executing S3 Bucket Content Clear", e)
    }
  }
}

return this;