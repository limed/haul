@Library('nubis') import org.mozilla.nubis.Static

def nubisStatic = new org.mozilla.nubis.Static()

node {
   stage('Prep') {
    checkout([$class: 'GitSCM', branches: [[name: '*/master']], doGenerateSubmoduleConfigurations: false, extensions: [[$class: 'RelativeTargetDirectory', relativeTargetDir: 'src/'], [$class: 'SubmoduleOption', disableSubmodules: false, parentCredentials: false, recursiveSubmodules: true, reference: '', trackingSubmodules: true], [$class: 'CleanBeforeCheckout']], submoduleCfg: [], userRemoteConfigs: [[url: 'https://github.com/mozilla/www.ccadb.org.git']]])
   }

  stage ('Build') {
    docker.image('jekyll/builder:3.6.2').inside('-e https_proxy=$HTTPS_PROXY -e HTTPS_PROXY -e http_proxy=$HTTP_PROXY -e HTTP_PROXY  --volume=$WORKSPACE:/srv/jekyll') {
      sh "cd /srv/jekyll"
      sh "ls"
      sh "pwd && DEBUG=true /usr/jekyll/bin/entrypoint jekyll -v"
      sh "pwd && DEBUG=true PAGES_REPO_NWO=mozilla/www.ccadb.org /usr/jekyll/bin/entrypoint jekyll build --verbose  -d ../dst"
    }
  }

  stage('Sync') {
     nubisStatic.syncSite()
  }
}


/usr/jekyll/bin/entrypoint