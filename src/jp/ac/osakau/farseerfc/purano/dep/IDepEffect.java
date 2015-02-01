package jp.ac.osakau.farseerfc.purano.dep;

import jp.ac.osakau.farseerfc.purano.effect.ArgumentEffect;
import jp.ac.osakau.farseerfc.purano.effect.CallEffect;
import jp.ac.osakau.farseerfc.purano.effect.Effect;
import jp.ac.osakau.farseerfc.purano.effect.FieldEffect;
import jp.ac.osakau.farseerfc.purano.effect.OtherFieldEffect;
import jp.ac.osakau.farseerfc.purano.effect.StaticEffect;

public interface IDepEffect {
	public void addThisField(FieldEffect tfe);
	public void addOtherField(OtherFieldEffect ofe);
	public void addOtherEffect(Effect oe);
	public void addCallEffect(CallEffect ce);
	public void addStaticField(StaticEffect sfe);
	public void addArgumentEffect(ArgumentEffect ae);
}
