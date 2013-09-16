package jp.ac.osakau.farseerfc.purano.reflect;

import com.google.common.base.Joiner;
import jp.ac.osakau.farseerfc.purano.ano.Purity;
import jp.ac.osakau.farseerfc.purano.util.Types;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: farseerfc
 * Date: 9/16/13
 * Time: 5:03 PM
 * To change this template use File | Settings | File Templates.
 */
public class LegacyDumpper implements ClassFinderDumpper {
    private final ClassFinder cf;

    public LegacyDumpper(ClassFinder cf) {
        this.cf = cf;
    }

    @Override
    public void dump(){
        Types table = new Types(true, cf.prefix.get(0));
        int method = 0, unknown = 0, stateless = 0, stateful = 0, modifier =0 ;
        int hmethod = 0, hunknown = 0, hstateless = 0, hstateful = 0, hmodifier =0, esln = 0 , esfn=0, en = 0 ;
        int emethod = 0, eunknown = 0, estateless = 0, estateful = 0, emodifier =0, hsln = 0 , hsfn=0, hn = 0 ;
        int fieldM = 0, staticM = 0, argM = 0, nativeE = 0;
        int classes = 0;

        List<String> sb = new ArrayList<>();

        for(String clsName : cf.classMap.keySet()){
            boolean isTarget = cf.classTargets.contains(clsName);
            for(String p:cf.prefix){
                if(clsName.startsWith(p)){
                    isTarget = true;
                }
            }
            for(MethodRep mtd: cf.classMap.get(clsName).getAllMethods()){
                int p=mtd.purity();
                if(mtd.getInsnNode().name.equals("equals") && mtd.getInsnNode().desc.equals("(Ljava/lang/Object;)Z")){
                    emethod++;
                    if(p == Purity.Unknown){
                        eunknown ++;
                    }
                    if(p == Purity.Stateless){
                        estateless ++;
                        sb.add("Equals Stateless:" + mtd.toString(new Types()));
                    }else if(p == Purity.Stateful){
                        estateful ++;
                        sb.add("Equals Stateful:" + mtd.toString(new Types()));
                    }else{
                        emodifier ++;
                        sb.add("Equals Motifier:" + mtd.toString(new Types()));
                        if(p==(Purity.Stateless | Purity.Native)){
                            esln ++ ;
                        }else if (p==(Purity.Stateless | Purity.Native)){
                            esfn ++ ;
                        }

                        if((p | Purity.Native)>0){
                            en++;
                        }
                    }
                }
                if(mtd.getInsnNode().name.equals("hashCode")&& mtd.getInsnNode().desc.equals("()I")){
                    hmethod++;
                    if(p == Purity.Unknown){
                        hunknown ++;
                    }
                    if(p == Purity.Stateless){
                        hstateless ++;
                        sb.add("hashCode Stateless:" + mtd.toString(new Types()));
                    }else if(p == Purity.Stateful){
                        hstateful ++;
                        sb.add("hashCode Stateful:" + mtd.toString(new Types()));
                    }else{
                        hmodifier ++;
                        sb.add("hashCode Modifier:" + mtd.toString(new Types()));
                        if(p==(Purity.Stateless | Purity.Native)){
                            hsln ++ ;
                        }else if (p==(Purity.Stateless | Purity.Native)){
                            hsfn ++ ;
                        }
                        if((p | Purity.Native)>0){
                            hn++;
                        }
                    }
                }
            }
            if (!isTarget) {
                continue;
            }
            ClassRep cls = cf.classMap.get(clsName);
            System.out.println(Joiner.on("\n").join(cls.dump(table)));
            for(MethodRep mtd: cls.getAllMethods()){
                method++;
                int p=mtd.purity();
                if(p == Purity.Unknown){
                    unknown ++;
                }
                if(p == Purity.Stateless){
                    stateless ++;
                }else if(p == Purity.Stateful){
                    stateful ++;
                }else{
                    modifier ++;
                }
                if((p & Purity.ArgumentModifier)>0){
                    argM ++;
                }
                if((p & Purity.FieldModifier)>0){
                    fieldM ++;
                }
                if((p & Purity.StaticModifier)>0){
                    staticM ++;
                }
                if((p & Purity.Native)>0){
                    nativeE ++;
                }

            }
            classes ++;
        }

        System.out.print(table.dumpImports());


        System.out.println("class " + classes);
        System.out.println("method "+method);
        System.out.println("unknown "+unknown);
        System.out.println("stateless "+stateless);
        System.out.println("stateful "+stateful);
        System.out.println("modifier "+modifier);

        System.out.println("fieldM "+fieldM);
        System.out.println("staticM "+staticM);
        System.out.println("argM "+argM);
        System.out.println("nativeE "+nativeE);

        System.out.println("emethod "+emethod);
        System.out.println("eunknown "+eunknown);
        System.out.println("estateless "+estateless);
        System.out.println("estateful "+estateful);
        System.out.println("emodifier "+emodifier);
        System.out.println("esln "+esln);
        System.out.println("esfn "+esfn);
        System.out.println("en "+en);

        System.out.println("emethod "+hmethod);
        System.out.println("eunknown "+hunknown);
        System.out.println("estateless "+hstateless);
        System.out.println("estateful "+hstateful);
        System.out.println("emodifier "+hmodifier);
        System.out.println("hsln "+hsln);
        System.out.println("hsfn "+hsfn);
        System.out.println("hn "+hn);

        System.out.println(Joiner.on("\n").join(sb));
    }
}
