
package sonarzero.gui;

import java.awt.Color;
import java.awt.Dimension;

/**
 *
 * @author terro
 */
public class ProjectPanel extends javax.swing.JPanel {

    /**
     * Creates new form ProjectPanel
     */
    public ProjectPanel(String text) {
        initComponents();
        border.setBackground(new Color(1,1,1,0.1f));
        javax.swing.GroupLayout thisLayout = new javax.swing.GroupLayout(this);
        this.setLayout(thisLayout);
        
        thisLayout.setHorizontalGroup(
            thisLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 232, Short.MAX_VALUE)
        );
        thisLayout.setVerticalGroup(
            thisLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 111, Short.MAX_VALUE)
        );
        title.setText("  "+text);
        

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        option = new javax.swing.JButton();
        openProject = new javax.swing.JButton();
        border = new javax.swing.JLabel();
        title = new javax.swing.JLabel();
        imagen = new javax.swing.JLabel();

        setBackground(new java.awt.Color(153, 153, 153));
        setMaximumSize(new java.awt.Dimension(232, 111));
        setMinimumSize(new java.awt.Dimension(232, 111));
        setLayout(null);

        option.setBackground(new java.awt.Color(51, 51, 51));
        option.setForeground(new java.awt.Color(204, 204, 204));
        option.setText("...");
        option.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                optionActionPerformed(evt);
            }
        });
        add(option);
        option.setBounds(170, 70, 40, 23);

        openProject.setBackground(new java.awt.Color(51, 51, 51));
        openProject.setForeground(new java.awt.Color(204, 204, 204));
        openProject.setText("Abrir Proyecto");
        openProject.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openProjectActionPerformed(evt);
            }
        });
        add(openProject);
        openProject.setBounds(20, 70, 150, 23);

        border.setBackground(new java.awt.Color(0, 0, 0));
        border.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        border.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        border.setBorder(new javax.swing.border.MatteBorder(null));
        border.setOpaque(true);
        add(border);
        border.setBounds(10, 10, 210, 90);

        title.setBackground(new java.awt.Color(0, 0, 0));
        title.setForeground(new java.awt.Color(255, 255, 255));
        title.setText("  Titulo del Proyecto");
        title.setOpaque(true);
        add(title);
        title.setBounds(10, 10, 210, 20);

        imagen.setIcon(new javax.swing.ImageIcon(getClass().getResource("/sonarzero/gui/assets/img/fondo.jpg"))); // NOI18N
        add(imagen);
        imagen.setBounds(-6, -6, 240, 120);
    }// </editor-fold>//GEN-END:initComponents

    private void optionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_optionActionPerformed
        getParent().remove(this);
        NewJFrame.i-=1;
        NewJFrame.ListProjects.setPreferredSize(new Dimension(
                NewJFrame.ListProjects.getSize().width,
                this.getSize().height*NewJFrame.i)
        );
        NewJFrame.ListProjects.updateUI();
    }//GEN-LAST:event_optionActionPerformed

    private void openProjectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openProjectActionPerformed
        System.out.println(""+title.getText());
    }//GEN-LAST:event_openProjectActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    public javax.swing.JLabel border;
    public javax.swing.JLabel imagen;
    public javax.swing.JButton openProject;
    public javax.swing.JButton option;
    public javax.swing.JLabel title;
    // End of variables declaration//GEN-END:variables
}
