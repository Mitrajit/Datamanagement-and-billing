/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mydbmanager;

import java.awt.event.KeyEvent;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.NumberFormat;
import javafx.scene.input.KeyCode;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Mitrajit
 */
public class FindStock extends javax.swing.JFrame {

    /**
     * Creates new form FindStock
     */
    Connection conn=null;
    PreparedStatement pst=null;
    ResultSet rs=null;
    public FindStock() {
        initComponents();
        try{
            Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
            conn=DriverManager.getConnection("jdbc:ucanaccess://E:\\Data entry_Project\\Mydb.accdb");
        }
        catch(Exception e)
        {
            JOptionPane.showMessageDialog(null, "Problem in connection");
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane2 = new javax.swing.JScrollPane();
        stocktable = new javax.swing.JTable();
        searchtext = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Find Stock");
        addWindowFocusListener(new java.awt.event.WindowFocusListener() {
            public void windowGainedFocus(java.awt.event.WindowEvent evt) {
                formWindowGainedFocus(evt);
            }
            public void windowLostFocus(java.awt.event.WindowEvent evt) {
            }
        });

        stocktable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Group", "Item", "Selling price", "Min. selling price", "MRP", "Qut. avail"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane2.setViewportView(stocktable);
        if (stocktable.getColumnModel().getColumnCount() > 0) {
            stocktable.getColumnModel().getColumn(0).setPreferredWidth(100);
            stocktable.getColumnModel().getColumn(1).setPreferredWidth(180);
            stocktable.getColumnModel().getColumn(5).setPreferredWidth(120);
        }

        searchtext.setFont(new java.awt.Font("Tahoma", 0, 13)); // NOI18N
        searchtext.addCaretListener(new javax.swing.event.CaretListener() {
            public void caretUpdate(javax.swing.event.CaretEvent evt) {
                searchtextCaretUpdate(evt);
            }
        });
        searchtext.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                searchtextKeyPressed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(searchtext)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 718, Short.MAX_VALUE))
                .addGap(15, 15, 15))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(26, 26, 26)
                .addComponent(searchtext, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(26, 26, 26)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 232, Short.MAX_VALUE)
                .addGap(18, 18, 18))
        );

        setSize(new java.awt.Dimension(764, 363));
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    public String Format(double a){
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(2);
        nf.setMinimumFractionDigits(2);
        nf.setGroupingUsed(false);
        return nf.format(a);
    }
    private boolean fillstocktable()
    {
        try{
            DefaultTableModel stocktm = (DefaultTableModel) stocktable.getModel();
            stocktm.setRowCount(0);
        String sql="SELECT * FROM Stock WHERE Item LIKE '"+searchtext.getText()+"*'";
        rs=conn.prepareStatement(sql).executeQuery();
        if(!rs.next())
        {
            sql="SELECT * FROM Stock WHERE [Group] LIKE '"+searchtext.getText()+"*'";
            rs=conn.prepareStatement(sql).executeQuery();
            rs.next();
        }
        do{
            stocktm.addRow(new Object[]{rs.getString("Group"),rs.getString("Item"),Format(rs.getDouble("Selling_price")),
                Format(rs.getDouble("Minsell_price")),Format(rs.getDouble("MRP")),
                (rs.getInt("Qut_available")/12)+"DOZ - "+(rs.getInt("Qut_available")-(rs.getInt("Qut_available")/12)*12)+"PCS"});
        }while(rs.next());
        }
        catch(Exception e)
        {   if(e.toString().equals("net.ucanaccess.jdbc.UcanaccessSQLException: UCAExc:::5.0.0-SNAPSHOT invalid cursor state: identifier cursor not positioned on row in UPDATE, DELETE, SET, or GET statement: ; ResultSet is empty"))
                return false;
            else
                JOptionPane.showMessageDialog(null, e); 
        }
        return true;
    }
    private void searchtextCaretUpdate(javax.swing.event.CaretEvent evt) {//GEN-FIRST:event_searchtextCaretUpdate
        // TODO add your handling code here:
        fillstocktable();
    }//GEN-LAST:event_searchtextCaretUpdate

    private void searchtextKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_searchtextKeyPressed
        // TODO add your handling code here:
        if(evt.getKeyCode()==KeyEvent.VK_ENTER)
        {
            if(!fillstocktable())
            JOptionPane.showMessageDialog(null, "No Item or Group found with this name");
        }
    }//GEN-LAST:event_searchtextKeyPressed

    private void formWindowGainedFocus(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowGainedFocus
        // TODO add your handling code here:
        fillstocktable();
    }//GEN-LAST:event_formWindowGainedFocus

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(FindStock.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(FindStock.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(FindStock.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(FindStock.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new FindStock().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextField searchtext;
    private javax.swing.JTable stocktable;
    // End of variables declaration//GEN-END:variables
}
