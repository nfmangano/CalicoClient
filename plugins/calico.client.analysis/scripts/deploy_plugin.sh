echo "**************** Start deploy ****************"
echo "**************** Building the plugin ****************"
export JAVA_HOME=/usr/lib/jvm/jdk1.6.0_32
ant clean
ant dist
CALICOCLIENT_HOME=/home/motta/Desktop/Calico/Calico-Analysis/Calico-Analysis-Client/
echo "**************** Copy into calico client plugin folder ****************"
cp dist/calico.client.analysis-trunk/calico.client.analysis.jar $CALICOCLIENT_HOME/trunk/calico3client-bugfixes/plugins/
echo "**************** Copy into calico client lib folder ****************"
cp dist/calico.client.analysis-trunk/calico.client.analysis.jar $CALICOCLIENT_HOME/trunk/calico3client-bugfixes/lib/
echo "**************** Deploy completed ****************"