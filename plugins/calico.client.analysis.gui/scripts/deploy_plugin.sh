echo "**************** Start deploy ****************"
echo "**************** Building the plugin ****************"
export JAVA_HOME=/usr/lib/jvm/jdk1.6.0_32
ant clean
ant dist
CALICOCLIENT_HOME=/home/motta//Desktop/Calico/CalicoSoft/CalicoClient/trunk/calico3client-bugfixes/
echo "**************** Copy into calico client plugin folder ****************"
cp dist/analysispluginclient-trunk/analysispluginclient.jar $CALICOCLIENT_HOME/plugins/
echo "**************** Copy into calico client lib folder ****************"
cp dist/analysispluginclient-trunk/analysispluginclient.jar $CALICOCLIENT_HOME/lib/
echo "**************** Deploy completed ****************"