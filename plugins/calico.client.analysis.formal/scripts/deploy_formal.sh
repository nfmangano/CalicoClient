echo "**************** Start deploy ****************"
export JAVA_HOME=/usr/lib/jvm/jdk1.6.0_32
ant -v clean
ant -v dist

#Every one is calico project needs to have the jar of the component that is using
#in its lib folder. This is why I copy this jar in the lib folders
#of the people using it: calico.analysis.gui and CalicoClient

echo "**************** Copy into calico.analysis.gui folder ****************"
CALICO_ANALYSIS_GUI_HOME=/home/motta//Desktop/Calico/CalicoSoft/calico.analysis.gui/
cp dist/calico.analysis.formal.jar $CALICO_ANALYSIS_GUI_HOME/lib/

echo "**************** Copy into calico client lib folder ****************"
CALICOCLIENT_HOME=/home/motta//Desktop/Calico/CalicoSoft/CalicoClient/trunk/calico3client-bugfixes/
cp dist/calico.analysis.formal.jar $CALICOCLIENT_HOME/lib/
echo "**************** End deploy ****************"