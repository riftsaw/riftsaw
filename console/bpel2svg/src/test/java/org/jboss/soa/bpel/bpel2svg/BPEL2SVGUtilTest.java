/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and others contributors as indicated
 * by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */
package org.jboss.soa.bpel.bpel2svg;

import junit.framework.TestCase;

import org.wso2.carbon.bpel.ui.bpel2svg.ActivityInterface;
import org.wso2.carbon.bpel.ui.bpel2svg.AssignInterface;
import org.wso2.carbon.bpel.ui.bpel2svg.IfInterface;
import org.wso2.carbon.bpel.ui.bpel2svg.ReceiveInterface;
import org.wso2.carbon.bpel.ui.bpel2svg.ReplyInterface;
import org.wso2.carbon.bpel.ui.bpel2svg.SVGInterface;
import org.wso2.carbon.bpel.ui.bpel2svg.SequenceInterface;
import org.wso2.carbon.bpel.ui.bpel2svg.ThrowInterface;
import org.wso2.carbon.bpel.ui.bpel2svg.WhileInterface;
import org.wso2.carbon.bpel.ui.bpel2svg.ActivityInterface.ActivityState;

public class BPEL2SVGUtilTest extends TestCase {

	public void testGenerateEventWorkFlowNoTrace() {
		
		java.io.InputStream is=ClassLoader.getSystemResourceAsStream("bpel/EventWorkFlow.bpel");
		
		if (is == null) {
			fail("BPEL process not found");
		}
		
		SVGInterface svg=null;
		
		try {
			svg = BPEL2SVGUtil.generate(is);
			
			if (svg == null) {
				fail("Failed to get SVGInterface");
			}
		} catch(Exception e) {
			fail("Failed: "+e);
		}
		
		// Serialize
		SVGImageTransformer transformer=BPEL2SVGUtil.getTransformer(BPEL2SVGUtil.SVG_IMAGE);
		String svgxml=null;
		
		try {
			java.io.ByteArrayOutputStream os=new java.io.ByteArrayOutputStream();
			transformer.transform(svg, os);
			
			os.close();
			
			svgxml = new String(os.toByteArray());
			
		} catch(Exception e) {
			e.printStackTrace();
			fail("Failed to generate SVG XML doc: "+e);
		}
		
		// Compare to saved svg image
		try {
			java.io.InputStream svgfile=ClassLoader.getSystemResourceAsStream("svg/EventWorkFlow-no-trace.svg");
			byte[] b=new byte[svgfile.available()];
			
			svgfile.read(b);
			
			svgfile.close();
			
			String savedsvg=new String(b);
			
			if (savedsvg.equals(svgxml) == false) {
				showDiffs(savedsvg, svgxml);

				fail("SVG images did not match");
			}
		} catch(Exception e) {
			fail("Compare failed: "+e);
		}
	}

	public void testGenerateMathNoTrace() {
		
		java.io.InputStream is=ClassLoader.getSystemResourceAsStream("bpel/Math.bpel");
		
		if (is == null) {
			fail("BPEL process not found");
		}
		
		SVGInterface svg=null;
		
		try {
			svg = BPEL2SVGUtil.generate(is);
			
			if (svg == null) {
				fail("Failed to get SVGInterface");
			}
		} catch(Exception e) {
			fail("Failed: "+e);
		}
		
		// Serialize
		SVGImageTransformer transformer=BPEL2SVGUtil.getTransformer(BPEL2SVGUtil.SVG_IMAGE);
		String svgxml=null;
		
		try {
			java.io.ByteArrayOutputStream os=new java.io.ByteArrayOutputStream();
			transformer.transform(svg, os);
			
			os.close();
			
			svgxml = new String(os.toByteArray());
			
		} catch(Exception e) {
			fail("Failed to generate SVG XML doc: "+e);
		}
		
		// Compare to saved svg image
		try {
			java.io.InputStream svgfile=ClassLoader.getSystemResourceAsStream("svg/Math-no-trace.svg");
			byte[] b=new byte[svgfile.available()];
			
			svgfile.read(b);
			
			svgfile.close();
			
			String savedsvg=new String(b);
			
			if (savedsvg.equals(svgxml) == false) {
				showDiffs(savedsvg, svgxml);

				fail("SVG images did not match");
			}
		} catch(Exception e) {
			fail("Compare failed: "+e);
		}
		
	}
	
	/*
	public void testGenerateMathRecvCompletedAssignFailed() {
		
		java.io.InputStream is=ClassLoader.getSystemResourceAsStream("bpel/Math.bpel");
		
		if (is == null) {
			fail("BPEL process not found");
		}
		
		SVGInterface svg=null;
		
		try {
			svg = BPEL2SVGUtil.generate(is);
			
			if (svg == null) {
				fail("Failed to get SVGInterface");
			}
		} catch(Exception e) {
			fail("Failed: "+e);
		}
		
		ActivityInterface seq=svg.getActivityAtLineNumber(53);
		ActivityInterface recv=svg.getActivityAtLineNumber(61);
		ActivityInterface assign=svg.getActivityAtLineNumber(64);
		
		seq.setState(ActivityState.Active);
		recv.setState(ActivityState.Completed);
		assign.setState(ActivityState.Active);
		
		// Serialize
		SVGImageTransformer transformer=BPEL2SVGUtil.getTransformer(BPEL2SVGUtil.SVG_IMAGE);
		String svgxml=null;
		
		try {
			java.io.ByteArrayOutputStream os=new java.io.ByteArrayOutputStream();
			transformer.transform(svg, os);
			
			os.close();
			
			svgxml = new String(os.toByteArray());
			
		} catch(Exception e) {
			fail("Failed to generate SVG XML doc: "+e);
		}
		
		// Compare to saved svg image
		try {
			java.io.InputStream svgfile=ClassLoader.getSystemResourceAsStream("svg/Math-recv-completed-assign-active.svg");
			byte[] b=new byte[svgfile.available()];
			
			svgfile.read(b);
			
			svgfile.close();
			
			String savedsvg=new String(b);
			
			if (savedsvg.equals(svgxml) == false) {
				showDiffs(savedsvg, svgxml);

				fail("SVG images did not match");
			}
		} catch(Exception e) {
			fail("Compare failed: "+e);
		}
		
	}
	
	public void testGenerateMathRecvCompletedAssignActive() {
		
		java.io.InputStream is=ClassLoader.getSystemResourceAsStream("bpel/Math.bpel");
		
		if (is == null) {
			fail("BPEL process not found");
		}
		
		SVGInterface svg=null;
		
		try {
			svg = BPEL2SVGUtil.generate(is);
			
			if (svg == null) {
				fail("Failed to get SVGInterface");
			}
		} catch(Exception e) {
			fail("Failed: "+e);
		}
		
		ActivityInterface seq=svg.getActivityAtLineNumber(53);
		ActivityInterface recv=svg.getActivityAtLineNumber(61);
		ActivityInterface assign=svg.getActivityAtLineNumber(64);
		
		seq.setState(ActivityState.Failed);
		recv.setState(ActivityState.Completed);
		assign.setState(ActivityState.Failed);
		
		// Serialize
		SVGImageTransformer transformer=BPEL2SVGUtil.getTransformer(BPEL2SVGUtil.SVG_IMAGE);
		String svgxml=null;
		
		try {
			java.io.ByteArrayOutputStream os=new java.io.ByteArrayOutputStream();
			transformer.transform(svg, os);
			
			os.close();
			
			svgxml = new String(os.toByteArray());
			
		} catch(Exception e) {
			fail("Failed to generate SVG XML doc: "+e);
		}
		
		// Compare to saved svg image
		try {
			java.io.InputStream svgfile=ClassLoader.getSystemResourceAsStream("svg/Math-recv-completed-assign-failed.svg");
			byte[] b=new byte[svgfile.available()];
			
			svgfile.read(b);
			
			svgfile.close();
			
			String savedsvg=new String(b);
			
			if (savedsvg.equals(svgxml) == false) {
				showDiffs(savedsvg, svgxml);

				fail("SVG images did not match");
			}
		} catch(Exception e) {
			fail("Compare failed: "+e);
		}
		
	}
	*/
	
	public void testMathGetLineNum53Seq() {
		checkActivityAtLine("bpel/Math.bpel", 53, SequenceInterface.class);
	}
	
	public void testMathGetLineNum61PickReceive() {
		checkActivityAtLine("bpel/Math.bpel", 61, ReceiveInterface.class);
	}
	
	public void testMathGetLineNum64Assign() {
		checkActivityAtLine("bpel/Math.bpel", 64, AssignInterface.class);
	}
	
	public void testMathGetLineNum72AssignStill() {
		checkActivityAtLine("bpel/Math.bpel", 72, AssignInterface.class);
	}
	
	public void testMathGetLineNum88Switch() {
		checkActivityAtLine("bpel/Math.bpel", 88, IfInterface.class);
	}
	
	public void testMathGetLineNum118Sequence() {
		checkActivityAtLine("bpel/Math.bpel", 118, SequenceInterface.class);
	}
	
	public void testMathGetLineNum119Assign() {
		checkActivityAtLine("bpel/Math.bpel", 119, AssignInterface.class);
	}
	
	public void testMathGetLineNum132Sequence() {
		checkActivityAtLine("bpel/Math.bpel", 132, SequenceInterface.class);
	}
	
	public void testMathGetLineNum133Switch() {
		checkActivityAtLine("bpel/Math.bpel", 133, IfInterface.class);
	}
	
	public void testMathGetLineNum146Sequence() {
		checkActivityAtLine("bpel/Math.bpel", 146, SequenceInterface.class);
	}
	
	public void testMathGetLineNum147Assign() {
		checkActivityAtLine("bpel/Math.bpel", 147, AssignInterface.class);
	}
	
	public void testMathGetLineNum168Throw() {
		checkActivityAtLine("bpel/Math.bpel", 168, ThrowInterface.class);
	}
	
	public void testMathGetLineNum177Sequence() {
		checkActivityAtLine("bpel/Math.bpel", 177, SequenceInterface.class);
	}
	
	public void testMathGetLineNum178Assign() {
		checkActivityAtLine("bpel/Math.bpel", 178, AssignInterface.class);
	}
	
	public void testMathGetLineNum186While() {
		checkActivityAtLine("bpel/Math.bpel", 186, WhileInterface.class);
	}
	
	public void testMathGetLineNum188Assign() {
		checkActivityAtLine("bpel/Math.bpel", 188, AssignInterface.class);
	}
	
	public void testMathGetLineNum229Assign() {
		checkActivityAtLine("bpel/Math.bpel", 229, AssignInterface.class);
	}
	
	public void testMathGetLineNum253Reply() {
		checkActivityAtLine("bpel/Math.bpel", 253, ReplyInterface.class);
	}
	
	public void checkActivityAtLine(String filename, int lineNo, Class<?> type) {
		java.io.InputStream is=ClassLoader.getSystemResourceAsStream(filename);
		
		if (is == null) {
			fail("BPEL process '"+filename+"' not found");
		}
		
		SVGInterface svg=null;
		
		try {
			svg = BPEL2SVGUtil.generate(is);
			
			if (svg == null) {
				fail("Failed to get SVGInterface");
			}
		} catch(Exception e) {
			fail("Failed: "+e);
		}

		ActivityInterface act=svg.getActivityAtLineNumber(lineNo);
		
		if (act == null) {
			fail("Failed to get '"+type.getName()+"' at line "+lineNo);
		}
		
		if (type.isAssignableFrom(act.getClass()) == false) {
			fail("Returned activity is not a '"+type.getName()+"'");
		}
	}

	protected void showDiffs(String savedsvg, String svgxml) {
		System.err.println("svgxml len="+svgxml.length()+", saved len="+savedsvg.length());
		
		int mismatch=0;
		for (int i=0; i < svgxml.length(); i++) {
			if (savedsvg.charAt(i) != svgxml.charAt(i)) {
				System.out.println("Mismatch at "+i+" ("+savedsvg.charAt(i)+" != "+svgxml.charAt(i)+")");
				System.out.println(" "+savedsvg.substring(i-20, i+20)+"   :    "+svgxml.substring(i-20, i+20));
				if (mismatch++ > 10) {
					break;
				}
			}
		}
	}
}
