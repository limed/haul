@Library('nubis') import org.mozilla.nubis.Static

def nubisStatic = new org.mozilla.nubis.Static()

node {
   stage('Prep') {
    checkout([$class: 'GitSCM', branches: [[name: '*/master']], doGenerateSubmoduleConfigurations: false, extensions: [[$class: 'RelativeTargetDirectory', relativeTargetDir: 'src/'], [$class: 'SubmoduleOption', disableSubmodules: false, parentCredentials: false, recursiveSubmodules: true, reference: '', trackingSubmodules: true], [$class: 'CleanBeforeCheckout']], submoduleCfg: [], userRemoteConfigs: [[url: 'https://github.com/mozilla-iot/mozilla-iot.github.io.git']]])
    checkout([$class: 'GitSCM', branches: [[name: '*/gh-pages']], doGenerateSubmoduleConfigurations: false, extensions: [[$class: 'RelativeTargetDirectory', relativeTargetDir: 'src/wot/'], [$class: 'SubmoduleOption', disableSubmodules: false, parentCredentials: false, recursiveSubmodules: true, reference: '', trackingSubmodules: true], [$class: 'CleanBeforeCheckout']], submoduleCfg: [], userRemoteConfigs: [[url: 'https://github.com/mozilla-iot/wot.git']]])
    checkout([$class: 'GitSCM', branches: [[name: '*/master']], doGenerateSubmoduleConfigurations: false, extensions: [[$class: 'RelativeTargetDirectory', relativeTargetDir: 'src/schemas/'], [$class: 'SubmoduleOption', disableSubmodules: false, parentCredentials: false, recursiveSubmodules: true, reference: '', trackingSubmodules: true], [$class: 'CleanBeforeCheckout']], submoduleCfg: [], userRemoteConfigs: [[url: 'https://github.com/mozilla-iot/schemas.git']]])
checkout([$class: 'GitSCM', branches: [[name: '*/master']], doGenerateSubmoduleConfigurations: false, extensions: [[$class: 'RelativeTargetDirectory', relativeTargetDir: 'docs/'], [$class: 'SubmoduleOption', disableSubmodules: false, parentCredentials: false, recursiveSubmodules: true, reference: '', trackingSubmodules: true], [$class: 'CleanBeforeCheckout']], submoduleCfg: [], userRemoteConfigs: [[url: 'https://github.com/mozilla-iot/docs.git']]])
    nubisStatic.prepSite()
   }

  stage ('Build') {
    nubisStatic.buildSite()

    dir("docs") {
      docker.image('jekyll/jekyll:3.7').inside('-u 0:0 -e JEKYLL_UID=115 -e JEKYLL_GID=121 --volume="${PWD}:/srv/jekyll" -e https_proxy=$HTTPS_PROXY -e HTTPS_PROXY -e http_proxy=$HTTP_PROXY -e HTTP_PROXY') {
        sh "env"
        sh "bundler install"
        sh "/usr/jekyll/bin/entrypoint jekyll build"
      }
      sh "rsync -av --delete _site/ ../dst/docs/"
    }
  }

  stage('Sync') {
     nubisStatic.syncSite()
  }
}
