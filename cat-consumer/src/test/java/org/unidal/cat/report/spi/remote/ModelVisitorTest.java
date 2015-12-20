package org.unidal.cat.report.spi.remote;

import java.io.InputStream;

import org.junit.Test;
import org.unidal.cat.report.demo.IVisitor;
import org.unidal.cat.report.demo.entity.Demo;
import org.unidal.cat.report.demo.entity.X;
import org.unidal.cat.report.demo.entity.Y;
import org.unidal.cat.report.demo.entity.Z;
import org.unidal.cat.report.demo.transform.BaseVisitor;
import org.unidal.cat.report.demo.transform.DefaultSaxParser;
import org.unidal.cat.report.demo.transform.DefaultXmlBuilder;

public class ModelVisitorTest {
	@Test
	public void test() throws Exception {
		InputStream in = getClass().getResourceAsStream("demo.xml");
		Demo demo = DefaultSaxParser.parse(in);

		DefaultXmlBuilder builder = new DefaultXmlBuilder();

		demo.accept(new Str("+2", null));
		demo.accept(new Str("*2", null));
		demo.accept(new Str("-2", null));
		// builder.setVisitor(new Add(2, new Mul(2, new Sub(2, null))));
		demo.accept(builder);

		System.out.println(demo);
	}

	static class Add extends BaseVisitor {
		private IVisitor m_visitor;

		private int m_value;

		public Add(int value, IVisitor visitor) {
			m_value = value;
			m_visitor = (visitor != null ? visitor : this);
		}

		@Override
		public void visitX(X x) {
			x.setA(x.getA() + m_value);

			if (x.getY() != null) {
				x.getY().accept(m_visitor);
			}
		}

		@Override
		public void visitY(Y y) {
			y.setB(y.getB() + m_value);

			for (Z z : y.getZs()) {
				z.accept(m_visitor);
			}
		}

		@Override
		public void visitZ(Z z) {
			z.setC(z.getC() + m_value);
		}
	}

	static class Mul extends BaseVisitor {
		private IVisitor m_visitor;

		private int m_value;

		public Mul(int value, IVisitor visitor) {
			m_value = value;
			m_visitor = (visitor != null ? visitor : this);
		}

		@Override
		public void visitX(X x) {
			x.setA(x.getA() * m_value);

			if (x.getY() != null) {
				x.getY().accept(m_visitor);
			}
		}

		@Override
		public void visitY(Y y) {
			y.setB(y.getB() * m_value);

			for (Z z : y.getZs()) {
				z.accept(m_visitor);
			}
		}

		@Override
		public void visitZ(Z z) {
			z.setC(z.getC() * m_value);
		}
	}

	static class Sub extends BaseVisitor {
		private IVisitor m_visitor;

		private int m_value;

		public Sub(int value, IVisitor visitor) {
			m_value = value;
			m_visitor = (visitor != null ? visitor : this);
		}

		@Override
		public void visitX(X x) {
			x.setA(x.getA() + m_value);

			if (x.getY() != null) {
				x.getY().accept(m_visitor);
			}
		}

		@Override
		public void visitY(Y y) {
			y.setB(y.getB() + m_value);

			for (Z z : y.getZs()) {
				z.accept(m_visitor);
			}
		}

		@Override
		public void visitZ(Z z) {
			z.setC(z.getC() + m_value);
		}
	}

	static class Str extends BaseVisitor {
		private Str m_visitor;

		private String m_value;

		public Str(String value, Str visitor) {
			m_value = value;
			m_visitor = (visitor != null ? visitor : this);
		}

		public Str getVisitor() {
			return m_visitor == this ? null : m_visitor;
		}

		@Override
		public void visitX(X x) {
			x.setDesc(x.getDesc() == null ? m_value : x.getDesc() + m_value);

			if (x.getY() != null) {
				x.getY().accept(m_visitor);
				
				if (m_visitor.getVisitor() != null) {
					x.accept(m_visitor.getVisitor());
				}
			}
		}

		@Override
		public void visitY(Y y) {
			y.setDesc(y.getDesc() == null ? m_value : y.getDesc() + m_value);

			for (Z z : y.getZs()) {
				z.accept(m_visitor);

				if (m_visitor.getVisitor() != null) {
					y.accept(m_visitor.getVisitor());
				}

			}
		}

		@Override
		public void visitZ(Z z) {
			z.setDesc(z.getDesc() == null ? m_value : z.getDesc() + m_value);
			

			if (m_visitor.getVisitor() != null) {
				z.accept(m_visitor.getVisitor());
			}
		}

		@Override
		public String toString() {
			return "Str[value=" + m_value + "]";
		}
	}
}
