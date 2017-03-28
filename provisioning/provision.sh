#!/bin/bash

SCRIPTDIR=$(dirname $0)

echo "Running provisioning script"

echo "deb http://www.apache.org/dist/cassandra/debian 310x main" | sudo tee -a /etc/apt/sources.list.d/cassandra.sources.list
curl https://www.apache.org/dist/cassandra/KEYS | sudo apt-key add -
sudo add-apt-repository -y ppa:openjdk-r/ppa
sudo apt-get update
sudo apt-get install unzip
sudo apt-get install -y openjdk-8-jdk
sudo apt-get install -y openjfx
sudo apt-get install -y cassandra

# Set up Java
export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64
export PATH=$JAVA_HOME/bin:$PATH
echo 'export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64' >> /home/ubuntu/.bashrc
echo 'export PATH=$JAVA_HOME/bin:$PATH' >> /home/ubuntu/.bashrc

# Set up Titan
pushd /usr/local

# Download package
sudo wget http://s3.thinkaurelius.com/downloads/titan/titan-1.0.0-hadoop1.zip -O titan-1.0.0-hadoop1.zip
sudo unzip titan-1.0.0-hadoop1.zip
sudo mv titan-1.0.0-hadoop1 titan
sudo rm titan-1.0.0-hadoop1.zip

# Set up permissions
sudo mkdir titan/db
sudo chmod a+w titan/db
sudo chmod a+w titan/ext
sudo chmod a+w titan/log
sudo chmod a+w titan/log/*

# Bind the server port to all network adapters so we can reach it from the host
sudo sed "s/host: .*/host: 0.0.0.0/" -i titan/conf/gremlin-server/gremlin-server.yaml
# Use basic REST rather than websockets - note that the Gremlin console doesn't appear to have a setting for this, at least not with Tinkerpop 3.0.1
# that Titan Db uses
#sudo sed 's/channelizer: .*/channelizer: org.apache.tinkerpop.gremlin.server.channel.HttpChannelizer/' -i titan/conf/gremlin-server/gremlin-server.yaml

# Modify path
export PATH=/usr/local/titan/bin:$PATH
echo 'export PATH=/usr/local/titan/bin:$PATH' >> /home/ubuntu/.bashrc
popd

# sudo wget http://archive.apache.org/dist/spark/spark-2.1.0/spark-2.1.0-bin-hadoop2.7.tgz -O /usr/local/spark-2.1.0-bin-hadoop2.7.tgz
# sudo tar xfvz /usr/local/spark-2.1.0-bin-hadoop2.7.tgz -C /usr/local
# sudo mv /usr/local/spark-2.1.0-bin-hadoop2.7 /usr/local/spark
# sudo rm /usr/local/spark-2.1.0-bin-hadoop2.7.tgz
# sudo chown ubuntu:ubuntu -R /usr/local/spark
# export PATH=/usr/local/spark/bin:/usr/local/spark/sbin:$PATH
# echo 'export PATH=/usr/local/spark/bin:/usr/local/spark/sbin:$PATH' >> /home/ubuntu/.bashrc
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
