package sonarzero.api.ui.utils;

import java.awt.Color;
import java.awt.Dimension;
import javax.swing.JLabel;

/**
 *
 * @author terro
 */
public class Piano extends javax.swing.JPanel {

    private int Bw,Bh;
    Double doublenum;
    private int Nw,Nh;
    private int padding=0;
    /**
     * Creates new form Piano
     * @param d Dimension of Piano
     */
    public Piano(Dimension d) {
        initComponents();
        Bw=d.width/52;
        Bh=d.height-8;
        doublenum=Bh/1.61;
        Nw=Bw-3;
        padding=(d.width/52)-3;
        Nh=doublenum.intValue();
        paintKeys();
    }
    
    public Piano(Dimension d,int padding) {
        initComponents();
        
        Bw=d.width/52;
        Bh=d.height-8;
        Nw=Bw-3;
        doublenum=Bh/1.61;
        Nh=doublenum.intValue();
        this.padding=padding;
        System.out.println(d.toString()+" Bw="+Bw+" Bh="+Bh+" Nw="+Nw+" Nh="+Nh);
        paintKeys();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setBackground(new java.awt.Color(88, 85, 85));
        setLayout(null);
    }// </editor-fold>//GEN-END:initComponents

    
    private void paintKeys(){
        JLabel blanca,negra;
        int aux=1;
        for (int i = 0; i < 52; i++) {
            
            blanca=new JLabel();
            
            blanca.setOpaque(true);
            
            blanca.setBackground(Color.white);
            
            if(aux==2||aux==5||i==51){
                add(blanca);
                setBoundsKeys(blanca, blanca, i, false);
            }
            else {
                negra=new JLabel();
                negra.setOpaque(true);
                negra.setBackground(Color.black);
                if(aux==7)  
                    aux=0;
                add(negra);
                add(blanca);
                setBoundsKeys(blanca, negra, i, true);
            }
            
            blanca.setBorder(javax.swing.BorderFactory.createMatteBorder(1, 1, 7
                    , 1, new java.awt.Color(0, 0, 0)));
            
            aux++;
        }
        revalidate();
        updateUI();
    }
    public void setPadding(int padding){
        this.padding=padding;
    }
    
    public int getPadding(){
        return this.padding;
    }
    
    
    private void setBoundsKeys(JLabel blanca,JLabel negra, int iteracion, boolean isNegra){
        if(iteracion==0){
            blanca.setBounds(padding, 0, Bw, Bh);   
            if(isNegra)
                negra.setBounds(padding+10, 0, Nw, Nh);
        }
        else {
            blanca.setBounds(Bw*iteracion+padding, 0, Bw, Bh);
            if(isNegra)
                negra.setBounds((Bw*iteracion+padding)+10, 0, Nw, Nh);
        }
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
