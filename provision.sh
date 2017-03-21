#!/bin/bash

SCRIPTDIR=$(dirname $0)

echo "Running provisioning script"

sudo add-apt-repository -y ppa:openjdk-r/ppa
sudo apt-get update
sudo apt-get install unzip
sudo apt-get install -y openjdk-8-jdk

# Set up Java
export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64
export PATH=$JAVA_HOME/bin:$PATH
echo 'export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64' >> /home/vagrant/.bashrc
echo 'export PATH=$JAVA_HOME/bin:$PATH' >> /home/vagrant/.bashrc

# Set up Titan
pushd /usr/local
sudo wget http://s3.thinkaurelius.com/downloads/titan/titan-1.0.0-hadoop2.zip -O titan-1.0.0-hadoop2.zip
sudo unzip titan-1.0.0-hadoop2.zip
sudo mv titan-1.0.0-hadoop2 titan
sudo rm titan-1.0.0-hadoop2.zip
sudo mkdir titan/db
sudo chmod a+w titan/db
sudo chmod a+w titan/ext
export PATH=/usr/local/titan/bin:$PATH
echo 'export PATH=/usr/local/titan/bin:$PATH' >> /home/vagrant/.bashrc
popd

# sudo wget http://archive.apache.org/dist/spark/spark-2.1.0/spark-2.1.0-bin-hadoop2.7.tgz -O /usr/local/spark-2.1.0-bin-hadoop2.7.tgz
# sudo tar xfvz /usr/local/spark-2.1.0-bin-hadoop2.7.tgz -C /usr/local
# sudo mv /usr/local/spark-2.1.0-bin-hadoop2.7 /usr/local/spark
# sudo rm /usr/local/spark-2.1.0-bin-hadoop2.7.tgz
# sudo chown vagrant:vagrant -R /usr/local/spark
# export PATH=/usr/local/spark/bin:/usr/local/spark/sbin:$PATH
# echo 'export PATH=/usr/local/spark/bin:/usr/local/spark/sbin:$PATH' >> /home/vagrant/.bashrc
#
# pushd /usr/local/spark/conf
# echo SPARK_MASTER_WEBUI_PORT=9080 | sudo tee spark-env.sh > /dev/null
# echo SPARK_WORKER_WEBUI_PORT=9081 | sudo tee -a spark-env.sh > /dev/null
# sudo chmod a+x spark-env.sh
# popd
#
# start-master.sh -h localhost
# start-slave.sh spark://localhost:7077 -h localhost

echo "Done"
