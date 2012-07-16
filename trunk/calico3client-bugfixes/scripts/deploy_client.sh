echo "**************** Start deploy ****************"
echo "**************** Building the plugin ****************"
export JAVA_HOME=/usr/lib/jvm/jdk1.6.0_32
ant clean
ant compile-calico
ant dist
CALICOCLIENT_HOME=/home/motta/Desktop/Calico/Calico-Analysis/Calico-Analysis-Client/
echo "**************** Copy into calico client plugin folder ****************"
cp dist/calico3client-trunk/calico3client.jar $CALICOCLIENT_HOME/plugins/calico.client.analysis/lib
echo "**************** Deploy completed ****************"