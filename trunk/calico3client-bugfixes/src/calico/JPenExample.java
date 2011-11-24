/* \begin{verbatim} */
package calico;

import javax.swing.JFrame;
import javax.swing.JLabel;
import jpen.event.PenListener;
import jpen.PButtonEvent;
import jpen.PLevel;
import jpen.PenManager;
import jpen.PenProvider;
import jpen.PKindEvent;
import jpen.PLevelEvent;
import jpen.provider.Utils;
import jpen.PScrollEvent;

public class JPenExample
	implements PenListener{

	public static void main(String... args) throws Throwable{
		new JPenExample();
	}

	JPenExample(){
		JLabel l=new JLabel("Move the pen or mouse over me!");
		PenManager pm=new PenManager(l);
		pm.pen.addListener(this);

		JFrame f=new JFrame("JPen Example");
		f.getContentPane().add(l);
		f.setSize(300, 300);
		f.setVisible(true);
	}

	//@Override
	public void penButtonEvent(PButtonEvent ev) {
		System.out.println(ev);
	}
	//@Override
	public void penKindEvent(PKindEvent ev) {
		System.out.println(ev);
	}
	//@Override
	public void penLevelEvent(PLevelEvent ev) {
		System.out.println(ev);
	}
	//@Override
	public void penScrollEvent(PScrollEvent ev) {
		System.out.println(ev);
	}
	//@Override
	public void penTock(long availableMillis) {
		System.out.println("TOCK - available period fraction: "+availableMillis);
	}
}
/* \end{verbatim} */
