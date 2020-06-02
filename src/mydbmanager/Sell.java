/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mydbmanager;

import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Vector;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRTableModelDataSource;
import net.sf.jasperreports.view.JasperViewer;

/**
 *
 * @author Mitrajit
 */
public class Sell extends javax.swing.JFrame {

    /**
     * Creates new form Sell
     */
    Connection conn=null;
    PreparedStatement pst=null;
    ResultSet rs=null;
    ArrayList <String> names=new ArrayList<>();
     Date dt= new Date();
     int IVN;
    ArrayList <String> itemnames = new ArrayList<>();
    ArrayList <Double> sellingprice = new ArrayList<>();
    HashMap <String, Integer> quantityavailable = new HashMap<>();
    
    public Sell() {
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
    
    private void allocatestock(){
        try
        {
            itemnames.clear();
            sellingprice.clear();
            quantityavailable.clear();
            String sql="SELECT * FROM Stock";
            pst=conn.prepareStatement(sql);
            rs=pst.executeQuery();
            while(rs.next()){
                itemnames.add(rs.getString("Item"));
                sellingprice.add(rs.getDouble("Selling_price"));
                quantityavailable.put(rs.getString("Item"),rs.getInt("Qut_available"));
            }
        }
        catch(Exception e){JOptionPane.showMessageDialog(null, e);}
    }
    
    public void autofillstock(){
        int start = itemname.getText().length();
        if (start==0)
            return;
        int last = start;
        rate.setText(Format(0d));
        qutshow.setText(" ");
        for(int i=0;i<itemnames.size();i++)
        {
            String complete=itemnames.get(i);
            
            if(complete.toUpperCase().startsWith(itemname.getText().toUpperCase()))
            {
                itemname.setText(complete);
                last=complete.length();
                rate.setText(Format(sellingprice.get(i)));
                qutshow.setText(quantifier(quantityavailable.get(itemname.getText())));
                break;
            }
        }
        if(start<last)
        {
            itemname.setCaretPosition(last);
            itemname.moveCaretPosition(start);
        }        
    }
    public void calculate()
    {
        total.setText(Format(Double.parseDouble(rate.getText())*(Integer.parseInt(qut.getValue().toString())+
                Double.parseDouble(qutpc.getValue().toString())/12.0)));
    }
    public String quantifier(int quantity)
    {
        return ((quantity/12)+"DOZ - "+(quantity-(quantity/12)*12)+"PCS");
    }
    public int quantifier(String quantity)
    {
        return (Integer.parseInt(quantity.substring(0, quantity.indexOf('D')))*12+
                Integer.parseInt(quantity.substring(quantity.lastIndexOf(' ')+1, quantity.indexOf('P'))));
    }
    private void retriveIVN()
    {
        if(!ivno.isEnabled())
            return;
        try{
         String sql="SELECT last(Invoice_no) AS IVN FROM Sell";
            pst=conn.prepareStatement(sql);
            rs=pst.executeQuery();
            if(rs.next())
                IVN=rs.getInt("IVN")+1;
            else
                IVN=1;
            ivno.setText(Format(IVN));
        }
        catch(Exception e)
        {
            JOptionPane.showMessageDialog(null, e);
        }
    }
    public String Format(double a){
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(2);
        nf.setMinimumFractionDigits(2);
        nf.setGroupingUsed(false);
        return nf.format(a);
    }
    private String Format(int a){
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMinimumIntegerDigits(4);
        nf.setGroupingUsed(false);
        return nf.format(a);
    }
    public void summation()
    {
        double sum=0.0;
        for(int i=0;i<itemtable.getRowCount();i++)
            sum+=Double.parseDouble(itemtable.getValueAt(i, 4).toString());
        totallabel.setText(Format(sum));
        discamt.setText("-"+Format(Double.parseDouble(discpercent.getText())*sum/100));
        grandtotal.setText(Format(sum+Double.parseDouble(discamt.getText())));
    }
    public void Addrow(Object[] dataRow)
    {
        DefaultTableModel model=(DefaultTableModel) itemtable.getModel();
        model.addRow(dataRow);
    }
     private void allocate(String column)
    {
        try
        {
            names.clear();
            String sql="SELECT * FROM CustomerProfile";
            pst=conn.prepareStatement(sql);
            rs=pst.executeQuery();
            while(rs.next())
                names.add(rs.getString(column));
        }
        catch(Exception e){JOptionPane.showMessageDialog(null, e);}
    }
    private void autofill(String text,javax.swing.JTextField txt)
    {
        int start = text.length();
        int last = start;
        
        for(int i=0;i<names.size();i++)
        {
            String complete=names.get(i);
            
            if(complete.toUpperCase().startsWith(text.toUpperCase()))
            {
                txt.setText(complete);
                last=complete.length();
                break;
            }
        }
        if(start<last)
        {
            txt.setCaretPosition(last);
            txt.moveCaretPosition(start);
        }        
    }
    String company="";
    public void filldetails()
    {
        if(!custname.isEnabled())
            return;
       if(!(custname.getText().equals("CASH")||custname.getText().equals(""))){
        custname.setText(custname.getText());
            try {
                pst=conn.prepareStatement("SELECT * FROM CustomerProfile WHERE Customer_name='"+custname.getText()+"'");
                rs=pst.executeQuery();
                if(rs.next()){
                    address.setText(rs.getString("Address")+"    "+rs.getString("Pin"));
                    city.setText(rs.getString("City_town"));
                    phone.setText(rs.getString("Phone_no"));
                    emailid.setText(rs.getString("EmailID"));
                        company=rs.getString("Company");
                }
                else
                    JOptionPane.showMessageDialog(null, "Record not found");
            } catch (Exception e) {
                       JOptionPane.showMessageDialog(null, e);                    
            }
       }
    }
    
    public void fillpaydetails()
    {
        if(!custname2.getText().equals("")){
        custname2.setText(custname2.getText());
            try {
                pst=conn.prepareStatement("SELECT * FROM CustomerProfile WHERE Customer_name='"+custname2.getText()+"'");
                rs=pst.executeQuery();
                if(rs.next()){
                    address1.setText(rs.getString("Address")+"    "+rs.getString("Pin"));
                    city1.setText(rs.getString("City_town"));
                    phone1.setText(rs.getString("Phone_no"));
                    emailid1.setText(rs.getString("EmailID"));
                    company=rs.getString("Company");
                    complab2.setText(company);
                    due.setText(Format(rs.getDouble("Due")));
                }
                else
                {   JOptionPane.showMessageDialog(null, "Record not found");
                    address1.setText(" ");
                    city1.setText(" ");
                    phone1.setText(" ");
                    emailid1.setText(" ");
                    complab2.setText(" ");
                    due.setText("0.00");
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, e);
            }
        } 
    } 
    public void fillcusttable(){
        try{
        String sql="SELECT * FROM CustomerProfile";
        DefaultTableModel paytm= (DefaultTableModel) paytable.getModel();
        paytm.setRowCount(0);
        //paytm.setColumnIdentifiers(new Object[]{"Customer Name","Address","City","Phone number","EmailId","DUE"});
        pst=conn.prepareStatement(sql);
        rs=pst.executeQuery();
        while(rs.next()){
            paytm.addRow(new Object[]{rs.getString("Customer_name"),rs.getString("Address")+"  "+rs.getString("Pin"),rs.getString("City_town"),
                rs.getString("Phone_no"),rs.getString("EmailID"),Format(rs.getDouble("Due"))});
        }
        sql="SELECT Last(ID) AS payid FROM Pay";
        rs=conn.prepareStatement(sql).executeQuery();
        if(rs.next())
            payno.setText(Format(rs.getInt("payid")+1));
        else
            payno.setText(Format(1));
        }catch(Exception e){
            JOptionPane.showMessageDialog(null, e);
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

        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        buttonGroup1 = new javax.swing.ButtonGroup();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel2 = new javax.swing.JPanel();
        custname = new javax.swing.JTextField();
        address = new javax.swing.JLabel();
        date = new com.toedter.calendar.JDateChooser();
        add = new javax.swing.JButton();
        delete = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        city = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        itemname = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        rate = new javax.swing.JFormattedTextField();
        jLabel7 = new javax.swing.JLabel();
        qut = new javax.swing.JSpinner();
        jLabel8 = new javax.swing.JLabel();
        total = new javax.swing.JFormattedTextField();
        findinvoice = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        itemtable = new javax.swing.JTable();
        confirm = new javax.swing.JButton();
        print = new javax.swing.JButton();
        jLabel11 = new javax.swing.JLabel();
        totallabel = new javax.swing.JLabel();
        clear = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        ivno = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        discpercent = new javax.swing.JTextField();
        discamt = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        grandtotal = new javax.swing.JLabel();
        phone = new javax.swing.JLabel();
        emailid = new javax.swing.JLabel();
        qutshow = new javax.swing.JLabel();
        Unit = new javax.swing.JTextField();
        qutpc = new javax.swing.JSpinner();
        jTextField1 = new javax.swing.JTextField();
        jLabel15 = new javax.swing.JLabel();
        customer = new javax.swing.JRadioButton();
        cash = new javax.swing.JRadioButton();
        jPanel3 = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        paymode = new javax.swing.JComboBox<>();
        jScrollPane3 = new javax.swing.JScrollPane();
        paytable = new javax.swing.JTable();
        jLabel13 = new javax.swing.JLabel();
        due = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        amtpaid = new javax.swing.JTextField();
        txnno = new javax.swing.JTextField();
        jLabel17 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        custname2 = new javax.swing.JTextField();
        address1 = new javax.swing.JLabel();
        city1 = new javax.swing.JLabel();
        phone1 = new javax.swing.JLabel();
        emailid1 = new javax.swing.JLabel();
        receipt = new javax.swing.JCheckBox();
        complab2 = new javax.swing.JLabel();
        payno = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        date1 = new com.toedter.calendar.JDateChooser();

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane1.setViewportView(jTable1);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        addWindowFocusListener(new java.awt.event.WindowFocusListener() {
            public void windowGainedFocus(java.awt.event.WindowEvent evt) {
                formWindowGainedFocus(evt);
            }
            public void windowLostFocus(java.awt.event.WindowEvent evt) {
            }
        });

        custname.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                custnameFocusLost(evt);
            }
        });
        custname.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                custnameActionPerformed(evt);
            }
        });
        custname.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                custnameKeyPressed(evt);
            }
        });

        address.setText(" ");

        date.setDate(dt);
        date.setDateFormatString("dd-MM- yyyy");

        add.setText("Add");
        add.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addActionPerformed(evt);
            }
        });

        delete.setText("Delete");
        delete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteActionPerformed(evt);
            }
        });

        jLabel2.setText("Customer name");

        city.setText(" ");

        jLabel4.setText("Date");

        jLabel5.setText("Item Name");

        itemname.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                itemnameKeyPressed(evt);
            }
        });

        jLabel6.setText("Rate");

        rate.setText("0.00");
        rate.addCaretListener(new javax.swing.event.CaretListener() {
            public void caretUpdate(javax.swing.event.CaretEvent evt) {
                rateCaretUpdate(evt);
            }
        });
        rate.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                rateFocusLost(evt);
            }
        });
        rate.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                rateMouseClicked(evt);
            }
        });

        jLabel7.setText("Quantity");

        qut.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                qutStateChanged(evt);
            }
        });

        jLabel8.setText("Total price");

        total.setEditable(false);
        total.setText("0.00");
        total.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                totalActionPerformed(evt);
            }
        });

        findinvoice.setText("Find Invoice");
        findinvoice.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                findinvoiceActionPerformed(evt);
            }
        });

        itemtable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "No", "Item name", "Rate", "Quantity", "Total"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        itemtable.getTableHeader().setReorderingAllowed(false);
        jScrollPane2.setViewportView(itemtable);
        if (itemtable.getColumnModel().getColumnCount() > 0) {
            itemtable.getColumnModel().getColumn(0).setPreferredWidth(10);
            itemtable.getColumnModel().getColumn(1).setPreferredWidth(150);
        }

        confirm.setText("Confirm");
        confirm.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                confirmActionPerformed(evt);
            }
        });

        print.setEnabled(false);
        print.setText("Print");
        print.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                printActionPerformed(evt);
            }
        });

        jLabel11.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel11.setText("SubTotal");

        totallabel.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        totallabel.setText("0.00");

        clear.setLabel("Clear");
        clear.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearActionPerformed(evt);
            }
        });

        jLabel1.setText("Invoice number");

        ivno.setText("Invoice number");
        ivno.setMinimumSize(new java.awt.Dimension(40, 20));

        jLabel3.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabel3.setText("Discount");

        jLabel12.setText("Discount(%)");

        discpercent.setText("0");
        discpercent.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                discpercentFocusLost(evt);
            }
        });
        discpercent.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                discpercentKeyPressed(evt);
            }
        });

        discamt.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        discamt.setText("-0.00");

        jLabel14.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel14.setText("GRAND TOTAL");

        grandtotal.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        grandtotal.setText("0.00");

        phone.setText(" ");

        emailid.setText(" ");
        address.setText("");  city.setText("");  phone.setText("");  emailid.setText("");

        qutshow.setText(" ");

        Unit.setEditable(false);
        Unit.setText("DOZ");

        qutpc.setModel(new javax.swing.SpinnerNumberModel());
        qutpc.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                qutpcStateChanged(evt);
            }
        });

        jTextField1.setEditable(false);
        jTextField1.setText("PCS");

        jLabel15.setText("-");

        buttonGroup1.add(customer);
        customer.setText("Customer");
        customer.setSelected(true);
        customer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                customerActionPerformed(evt);
            }
        });

        buttonGroup1.add(cash);
        cash.setText("CASH");
        cash.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cashActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(20, 20, 20)
                        .addComponent(jScrollPane2))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jLabel14)
                                    .addGroup(jPanel2Layout.createSequentialGroup()
                                        .addComponent(jLabel12)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(discpercent, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(jLabel3))
                                    .addComponent(jLabel11))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(totallabel)
                                    .addComponent(discamt)
                                    .addComponent(grandtotal)))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(confirm, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(print, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(clear, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addGap(30, 30, 30))
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(11, 11, 11)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2)
                    .addComponent(jLabel5))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(emailid, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(phone, javax.swing.GroupLayout.DEFAULT_SIZE, 268, Short.MAX_VALUE)
                            .addComponent(city, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(ivno, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(address, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(cash)
                        .addGap(18, 18, 18)
                        .addComponent(customer))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(add, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(delete, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(findinvoice, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel4)
                                .addGap(14, 14, 14)
                                .addComponent(date, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(custname, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 185, Short.MAX_VALUE)
                            .addComponent(itemname, javax.swing.GroupLayout.Alignment.LEADING))
                        .addGap(26, 26, 26)
                        .addComponent(jLabel6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(rate, javax.swing.GroupLayout.DEFAULT_SIZE, 85, Short.MAX_VALUE)
                        .addGap(20, 20, 20)
                        .addComponent(jLabel7)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(qut, javax.swing.GroupLayout.DEFAULT_SIZE, 76, Short.MAX_VALUE)
                                .addGap(0, 0, 0)
                                .addComponent(Unit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel15)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(qutpc, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, 0)
                                .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jLabel8)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(total, javax.swing.GroupLayout.DEFAULT_SIZE, 76, Short.MAX_VALUE))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(qutshow)
                                .addGap(0, 0, Short.MAX_VALUE)))))
                .addGap(39, 39, 39))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel1)
                        .addComponent(ivno, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(customer)
                        .addComponent(cash)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(date, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel2Layout.createSequentialGroup()
                            .addGap(6, 6, 6)
                            .addComponent(jLabel2))
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(custname, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel4))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(address)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(city)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(phone)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(emailid)
                .addGap(0, 0, 0)
                .addComponent(qutshow)
                .addGap(3, 3, 3)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(itemname, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6)
                    .addComponent(jLabel7)
                    .addComponent(qut, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8)
                    .addComponent(total, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(rate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(Unit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(qutpc, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel15))
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(delete)
                    .addComponent(add)
                    .addComponent(findinvoice))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 234, Short.MAX_VALUE)
                .addGap(10, 10, 10)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel11)
                    .addComponent(totallabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jLabel12)
                    .addComponent(discpercent, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(discamt))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel14)
                    .addComponent(grandtotal))
                .addGap(12, 12, 12)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(print)
                    .addComponent(confirm)
                    .addComponent(clear))
                .addGap(25, 25, 25))
        );

        jTabbedPane1.addTab("Sell", jPanel2);

        jLabel9.setText("Customer name");

        jLabel10.setText("Payment mode");

        paymode.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Cash", "Cheque", "Card", "Mobile wallet/UPI", "Demand Draft", "Bank Transfer" }));
        paymode.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                paymodeActionPerformed(evt);
            }
        });

        paytable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Customer Name", "Address", "City", "Phone", "EmailId", "DUE"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        paytable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                paytableMouseClicked(evt);
            }
        });
        jScrollPane3.setViewportView(paytable);

        jLabel13.setText("Due amount");

        due.setFont(new java.awt.Font("DialogInput", 1, 14)); // NOI18N
        due.setText("0.00");

        jLabel16.setText("Amount");

        amtpaid.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                amtpaidFocusLost(evt);
            }
        });

        txnno.setVisible(false);

        jLabel17.setVisible(false);
        jLabel17.setText("Txn No.");

        jButton1.setText("Pay");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        custname2.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                custname2FocusLost(evt);
            }
        });
        custname2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                custname2ActionPerformed(evt);
            }
        });
        custname2.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                custname2KeyPressed(evt);
            }
        });

        address1.setText(" ");

        city1.setText(" ");

        phone1.setText(" ");

        emailid1.setText(" ");

        receipt.setText("Generate receipt");

        complab2.setText(" ");

        payno.setText("jLabel18");

        jLabel18.setText("Date");

        date1.setDate(dt);
        date1.setDateFormatString("dd-MM- yyyy");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(63, 63, 63)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel13)
                        .addGap(35, 35, 35)
                        .addComponent(due, javax.swing.GroupLayout.PREFERRED_SIZE, 208, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel9)
                        .addGap(18, 18, 18)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(payno)
                            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(complab2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(emailid1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(phone1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 208, Short.MAX_VALUE)
                                .addComponent(city1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(address1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(custname2, javax.swing.GroupLayout.Alignment.TRAILING)))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 184, Short.MAX_VALUE)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(receipt, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(82, 82, 82))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(jLabel18, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(date1, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jLabel10, javax.swing.GroupLayout.DEFAULT_SIZE, 92, Short.MAX_VALUE)
                                    .addComponent(jLabel16, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jLabel17, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(amtpaid, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(txnno, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(paymode, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addGap(72, 72, 72))))
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(jScrollPane3)
                .addGap(20, 20, 20))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(payno)
                        .addComponent(jLabel18))
                    .addComponent(date1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(paymode, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel10))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(amtpaid, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel16))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txnno, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel17)))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(custname2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel9))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(address1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(city1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(phone1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(emailid1)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(complab2)
                .addGap(7, 7, 7)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1)
                    .addComponent(due)
                    .addComponent(jLabel13)
                    .addComponent(receipt))
                .addGap(34, 34, 34)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 311, Short.MAX_VALUE)
                .addGap(46, 46, 46))
        );

        jTabbedPane1.addTab("Payment", jPanel3);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 636, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void totalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_totalActionPerformed
        // TODO add your handling code here:
        // create the formatters, default, display, edit
        //        NumberFormatter defaultFormatter = new NumberFormatter(new DecimalFormat("#.##"));
        //        NumberFormatter displayFormatter = new NumberFormatter(new DecimalFormat("#.##"));
        //        NumberFormatter editFormatter = new NumberFormatter(new DecimalFormat("#.##"));
        //        // set their value classes
        //        defaultFormatter.setValueClass(Double.class);
        //        displayFormatter.setValueClass(Double.class);
        //        editFormatter.setValueClass(Double.class);
        //        // create and set the DefaultFormatterFactory
        //        DefaultFormatterFactory valueFactory = new DefaultFormatterFactory(defaultFormatter,displayFormatter,editFormatter);
        //        jFormattedTextField1.setFormatterFactory(valueFactory);
    }//GEN-LAST:event_totalActionPerformed

    private void qutStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_qutStateChanged
        // TODO add your handling code here:
        //jFormattedTextField1.setText(Double.toString(Double.parseDouble(jSpinner1.getValue().toString()) * Double.parseDouble(jFormattedTextField2.getText())));
        //jFormattedTextField2CaretUpdate(evt);
        calculate();
    }//GEN-LAST:event_qutStateChanged

    private void rateMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_rateMouseClicked
        // TODO add your handling code here:
        if(rate.getText().equals("0.00")){
            rate.setCaretPosition(rate.getText().length());
            rate.moveCaretPosition(0);}
    }//GEN-LAST:event_rateMouseClicked

    private void rateFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_rateFocusLost
        // TODO add your handling code here:
        rate.setText(Format(Double.parseDouble(rate.getText())));
    }//GEN-LAST:event_rateFocusLost

    private void rateCaretUpdate(javax.swing.event.CaretEvent evt) {//GEN-FIRST:event_rateCaretUpdate
        // TODO add your handling code here:
        if(!(rate.getText().equals("")))
        calculate();
    }//GEN-LAST:event_rateCaretUpdate

    private void deleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteActionPerformed
        // TODO add your handling code here:
        DefaultTableModel mt=(DefaultTableModel) itemtable.getModel();
        //System.out.println((int)jTable1.getValueAt(jTable1.getRowCount()-1,0));
        while(itemtable.getSelectedRow()!=-1){
            try{
                quantityavailable.replace((String) itemtable.getValueAt(itemtable.getSelectedRow(), 1), 
                        quantityavailable.get(itemtable.getValueAt(itemtable.getSelectedRow(), 1))+quantifier(itemtable.getValueAt(itemtable.getSelectedRow(), 3).toString()));
            }catch(Exception e){}
            finally{
            mt.removeRow(itemtable.getSelectedRow());}}
        for(int i=0;i<itemtable.getRowCount();i++)
            itemtable.setValueAt(i+1, i, 0);
        //jLabel5.setText(nf.format(Integer.parseInt(itemtable.getValueAt(itemtable.getRowCount()-1,0).toString())+1));
        autofillstock();
        summation();
    }//GEN-LAST:event_deleteActionPerformed

    private void addActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addActionPerformed
        // TODO add your handling code here:
        int serialno;
        if(itemtable.getRowCount()==0)
            serialno=1;
        else
            serialno=Integer.parseInt(itemtable.getValueAt(itemtable.getRowCount()-1, 0).toString())+1;
        try{
        if(Integer.valueOf(qut.getValue().toString())*12+Integer.valueOf(qutpc.getValue().toString())<=quantityavailable.get(itemname.getText())){
        Addrow(new Object[]{serialno,itemname.getText(),rate.getText(),quantifier(Integer.valueOf(qut.getValue().toString())*12+
                Integer.valueOf(qutpc.getValue().toString())),total.getText()});
        quantityavailable.replace(itemname.getText(), quantityavailable.get(itemname.getText())-Integer.valueOf(qut.getValue().toString())*12+
                Integer.valueOf(qutpc.getValue().toString()));
        itemname.setText("");
        total.setText("0.00");
        rate.setText("0.00");
        qut.setValue(1);
        qutshow.setText("");
//        NumberFormat nf = NumberFormat.getInstance();
//        nf.setMinimumIntegerDigits(4);
//        nf.setGroupingUsed(false);
        //jLabel5.setText(nf.format(Integer.parseInt(jTable1.getValueAt(jTable1.getRowCount()-1,0).toString())+1));
        }
        else
            JOptionPane.showMessageDialog(null, "Stock not available");
        }
        catch (Exception e){
            JOptionPane.showMessageDialog(null, "Item not in stock");
        }
        summation();
    }//GEN-LAST:event_addActionPerformed

    private void custnameKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_custnameKeyPressed
        // TODO add your handling code here:
        switch(evt.getKeyCode())
        {
            case KeyEvent.VK_ENTER:
                filldetails();
            break;
            default:
                if((evt.getKeyChar()>=48&&evt.getKeyChar()<=57)||(evt.getKeyChar()>='A'&&evt.getKeyChar()<='Z')||(evt.getKeyChar()>='a'&&evt.getKeyChar()<='z')||evt.getKeyChar()==' ')
                EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                if(custname.getText().length()==1)
                allocate("Customer_name");
                
                 autofill(custname.getText(),custname);
            }
        });
        }
    }//GEN-LAST:event_custnameKeyPressed

    private void confirmActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_confirmActionPerformed
        // TODO add your handling code here:
        try
        {
            summation();
            if(!custname.getText().equals("")&&itemtable.getRowCount()!=0) {
                SimpleDateFormat dtfrmat=new SimpleDateFormat("yyyy-MM-dd");
                DefaultTableModel tm = (DefaultTableModel)itemtable.getModel();
                allocatestock();
                   for(int i=0;i<itemtable.getRowCount();i++){
                       try {
                           if(quantityavailable.get(itemtable.getValueAt(i, 1))>=quantifier(itemtable.getValueAt(i, 3).toString()))
                           quantityavailable.replace(itemtable.getValueAt(i, 1).toString(), quantityavailable.get(itemtable.getValueAt(i, 1))-quantifier(itemtable.getValueAt(i, 3).toString()));
                           else
                               throw new NullPointerException();
                       } catch (NullPointerException e) {
                           JOptionPane.showMessageDialog(null, "Some items may not be in the stock");
                           tm.setRowCount(0);
                           summation();
                           allocatestock();
                           return;
                       }
                   }
                   
                    String sql="SELECT * FROM CustomerProfile WHERE Customer_name='"+custname.getText()+"'";
                        if(customer.isSelected()){
                            try{
                            pst=conn.prepareStatement(sql);
                            rs=pst.executeQuery();
                            rs.next();
                            sql="UPDATE CustomerProfile SET Sale_amount='"+(rs.getDouble("Sale_amount")+Double.parseDouble(grandtotal.getText()))+"', Due='"
                                    +(rs.getDouble("Sale_amount")+Double.parseDouble(grandtotal.getText())-rs.getDouble("Paid"))+"' WHERE Customer_name='"+custname.getText()+"'";
                             pst=conn.prepareStatement(sql);
                             pst.executeUpdate();
                            }
                            catch(Exception e)
                            {
                                JOptionPane.showMessageDialog(null, "Customer name not found!");
                                return;
                            }
                        }
                        
                        for(int j=0;j<itemnames.size();j++)
                        {
                            sql="UPDATE Stock SET Qut_available = '"+quantityavailable.get(itemnames.get(j))+
                                    "' WHERE Item = '"+itemnames.get(j)+"'";
                            pst=conn.prepareStatement(sql);
                            pst.executeUpdate();}
                            
                        for(int i=0;i<itemtable.getRowCount();i++){
                            sql="INSERT INTO Sell (Invoice_no,[Date],Customer_name,Address,City_town,Item,Rate,Quantity,Unit,Total,Discount) VALUES ('"+IVN+"','"+
                            dtfrmat.format(date.getDate())+"','"+custname.getText()+"','"+address.getText()+"','"+city.getText()+"','"+
                            itemtable.getValueAt(i, 1).toString()+"','"+Double.parseDouble(itemtable.getValueAt(i, 2).toString())+"','"+
                            quantifier(itemtable.getValueAt(i, 3).toString())+"','"+
                            "PCS"+"','"+            itemtable.getValueAt(i, 4)+"','"+discpercent.getText()+"')";
                            pst=conn.prepareStatement(sql);
                            pst.executeUpdate();}
                        JOptionPane.showMessageDialog(null, "Added successfully");
                        itemname.setText("");
                        rate.setText("0.00");
                        qut.setValue(1);
                        total.setText("0.00");
                        custname.setEnabled(false);
                        ivno.setEnabled(false);
                        discpercent.setEnabled(false);
                        date.setEnabled(false);
                        add.setEnabled(false);
                        delete.setEnabled(false);
                        findinvoice.setEnabled(false);
                        confirm.setEnabled(false);
                        print.setEnabled(true);
                
            } else
                JOptionPane.showMessageDialog(null, "Fields are found empty.","Wrong exploitation",JOptionPane.ERROR_MESSAGE);
        }
        catch(Exception e)
        {
           JOptionPane.showMessageDialog(null, e);
                    
        }
    }//GEN-LAST:event_confirmActionPerformed

    private void clearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearActionPerformed
        // TODO add your handling code here:
        ivno.setEnabled(true);
        retriveIVN();
        if(customer.isSelected())
        custname.setText("");
        custname.setEnabled(true);
        address.setText("");
        city.setText("");
        phone.setText("");
        emailid.setText("");
        date.setEnabled(true);
        itemname.setText("");
        rate.setText("0.00");
        qut.setValue(1);
        qutshow.setText(" ");
        total.setText("0.00");
        totallabel.setText("0.00");
        discamt.setText("-0.00");
        discpercent.setEnabled(true);
        discpercent.setText("0");
        grandtotal.setText("0.00");
        add.setEnabled(true);
        delete.setEnabled(true);
        findinvoice.setEnabled(true);
        confirm.setEnabled(true);
        print.setEnabled(false);
        DefaultTableModel table= (DefaultTableModel)itemtable.getModel();
        table.setRowCount(0);
        date.setDate(dt);
    }//GEN-LAST:event_clearActionPerformed

    private void findinvoiceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_findinvoiceActionPerformed
        // TODO add your handling code here:
        try {
            boolean gotname=true;
            String sql="SELECT * FROM Sell WHERE Invoice_no='"+Integer.parseInt(ivno.getText())+"'";
            pst=conn.prepareStatement(sql);
            rs=pst.executeQuery();
            if(!rs.next()){
            SimpleDateFormat dtfrmat=new SimpleDateFormat("yyyy-MM-dd");
            sql="SELECT * FROM Sell WHERE [Date] = '"+dtfrmat.format(date.getDate())+"' AND Customer_name='"+custname.getText()+"'";
            pst=conn.prepareStatement(sql);
            rs=pst.executeQuery();
            if(!(rs.next()))
            {JOptionPane.showMessageDialog(null, "No record found");
            gotname=false;}
            }
            if(gotname){
                int serial=0;
                int lastivns=rs.getInt("Invoice_no");
                ivno.setText(Format(lastivns));
                custname.setText(rs.getString("Customer_name"));
//                address.setText(rs.getString("Address"));
//                city.setText(rs.getString("City_town"));
                date.setDate(rs.getDate("Date"));
                print.setEnabled(true);
                discpercent.setText(Double.toString(rs.getDouble("Discount")));
                ivno.setEnabled(false);
                DefaultTableModel table = (DefaultTableModel)itemtable.getModel();
                table.setRowCount(0);
                boolean notset=true;
            do 
            {
                if(rs.getInt("Invoice_no")!=lastivns){
                    lastivns=rs.getInt("Invoice_no");
                    ivno.setText(ivno.getText()+", "+Format(lastivns));
                    if(notset){
                    print.setEnabled(false);
                    ivno.setEnabled(true);
                    JOptionPane.showMessageDialog(null, "Multiple invoice is shown find one invoice using invoice number to enable "
                            + "printing and see the exact Grand total");
                    notset=false;
                    }
                }
                Object[] row={++serial,rs.getString("Item"),Format(rs.getDouble("Rate")),quantifier(rs.getInt("Quantity")),Format(rs.getDouble("Total"))};
                Addrow(row);
            }while(rs.next());
            filldetails();
            itemname.setText("");
            rate.setText("0.00");
            qut.setValue(1);
            total.setText("0.00");
            custname.setEnabled(false);
            discpercent.setEnabled(false);
            date.setEnabled(false);
            add.setEnabled(false);
            delete.setEnabled(false);
            confirm.setEnabled(false);
            summation();
            }
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);
        }
    }//GEN-LAST:event_findinvoiceActionPerformed

    private void printActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_printActionPerformed
        // TODO add your handling code here:
        summation();
        Vector<String> columnNames = new Vector<>();
        columnNames.add("No");
        columnNames.add("Item name");
        columnNames.add("Rate");
        columnNames.add("Quantity");
        columnNames.add("Total");       
        DefaultTableModel dtm= (DefaultTableModel) itemtable.getModel();
        int rowcount=dtm.getRowCount();
        DefaultTableModel dtm2= new DefaultTableModel(dtm.getDataVector(),columnNames);
        for(int i=dtm2.getRowCount();i<(int)(Math.ceil(rowcount/18.0d))*18;i++)
        dtm2.addRow(new Object[]{"","","","",""});
            HashMap<String, Object> para= new HashMap<>();
            para.put("Grandtotal",grandtotal.getText());
            para.put("Subtotal",totallabel.getText());
            para.put("discountper",discpercent.getText());
            para.put("discountamt",discamt.getText());
            para.put("custname",custname.getText());
            para.put("Company",company);
            para.put("Address",address.getText());
            para.put("City", city.getText());
            para.put("phone",phone.getText());
            para.put("emailid",emailid.getText());
            para.put("Invoice_no", ivno.getText());
            SimpleDateFormat dtfrmat=new SimpleDateFormat("dd-MM-yyyy");
            para.put("Date", dtfrmat.format(date.getDate()));
            try{
            JasperCompileManager.compileReportToFile("E:\\Data entry_Project\\MyDBmanager\\src\\mydbmanager\\Invoice.jrxml",
                    "E:\\Data entry_Project\\MyDBmanager\\src\\mydbmanager\\Invoice.jasper");
             JasperPrint  printing = (JasperPrint) JasperFillManager.fillReport("E:\\Data entry_Project\\MyDBmanager\\src\\mydbmanager"
                     + "\\Invoice.jasper",para,new JRTableModelDataSource(dtm2));
            JasperViewer.viewReport(printing,false);
            }
            catch(Exception e)
            {
                JOptionPane.showMessageDialog(null, e);
            }
            dtm.setRowCount(rowcount);
    }//GEN-LAST:event_printActionPerformed
    String prevdiscper="";
    private void discpercentKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_discpercentKeyPressed
        // TODO add your handling code here:
        //if(((evt.getKeyChar()>=48&&evt.getKeyChar()<=57))||evt.getKeyChar()=='.'||evt.getKeyCode()==Event.BACK_SPACE||evt.getKeyCode()==Event.ENTER)
           //give override
         
           EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                 try{
                     if(!discpercent.getText().equals("")){
                         NullPointerException nullPointer = new NullPointerException();
                         if((evt.getKeyChar()>='A'&&evt.getKeyChar()<='Z')||(evt.getKeyChar()>='a'&&evt.getKeyChar()<='z')||evt.getKeyChar()==32||
                                 Double.parseDouble(discpercent.getText())<0.0||Double.parseDouble(discpercent.getText())>100.0)
                            throw nullPointer; 
                        summation();
                        prevdiscper=discpercent.getText();}
                 }
                 catch(Exception e)
          {
              Toolkit.getDefaultToolkit().beep();
              discpercent.setText(prevdiscper);
          }
            }
            });
          
    }//GEN-LAST:event_discpercentKeyPressed

    private void custnameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_custnameActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_custnameActionPerformed

    private void discpercentFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_discpercentFocusLost
        // TODO add your handling code here:
        if(discpercent.getText().equals("")){
        discpercent.setText("0");
        summation();}
    }//GEN-LAST:event_discpercentFocusLost

    private void itemnameKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_itemnameKeyPressed
        // TODO add your handling code here:\
         if((evt.getKeyChar()>=48&&evt.getKeyChar()<=57)||(evt.getKeyChar()>='A'&&evt.getKeyChar()<='Z')||(evt.getKeyChar()>='a'&&evt.getKeyChar()<='z')||evt.getKeyChar()==' ')
                EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                autofillstock();
            }
        });
         else if(evt.getKeyCode()==KeyEvent.VK_ENTER)
             itemname.setText(itemname.getText());
    }//GEN-LAST:event_itemnameKeyPressed

    private void custnameFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_custnameFocusLost
        // TODO add your handling code here:
        filldetails();
    }//GEN-LAST:event_custnameFocusLost

    private void paymodeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_paymodeActionPerformed
        if(paymode.getSelectedItem().equals("Cash")){
            jLabel17.setVisible(false);  txnno.setVisible(false); txnno.setText("");}
        else {
            jLabel17.setVisible(true);  txnno.setVisible(true);
            if(paymode.getSelectedItem().equals("Demand Draft"))
              jLabel17.setText("DD No.");
            else if(paymode.getSelectedItem().equals("Card"))
                jLabel17.setText("Auth Code");
            else if(paymode.getSelectedItem().equals("Cheque"))
                jLabel17.setText("Cheque No.");
            else
                jLabel17.setText("Txn No.");
            }    
    }//GEN-LAST:event_paymodeActionPerformed

    private void custname2FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_custname2FocusLost
        fillpaydetails();
    }//GEN-LAST:event_custname2FocusLost

    private void custname2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_custname2ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_custname2ActionPerformed

    private void custname2KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_custname2KeyPressed
       switch(evt.getKeyCode())
        {
            case KeyEvent.VK_ENTER:
                fillpaydetails();
            break;
            default:
                if((evt.getKeyChar()>=48&&evt.getKeyChar()<=57)||(evt.getKeyChar()>='A'&&evt.getKeyChar()<='Z')||(evt.getKeyChar()>='a'&&evt.getKeyChar()<='z')||evt.getKeyChar()==' ')
                EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                if(custname2.getText().length()==1)
                allocate("Customer_name");
                
                 autofill(custname2.getText(),custname2);
            }
        });
        }
    }//GEN-LAST:event_custname2KeyPressed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        try {
            String sql="SELECT * FROM Customerprofile WHERE Customer_name='"+custname2.getText()+"'";
             pst=conn.prepareStatement(sql);
            rs=pst.executeQuery();
            rs.next();
            sql="UPDATE CustomerProfile SET Paid='"+(rs.getDouble("Paid")+Double.parseDouble(amtpaid.getText()))+"', Due='"
                                    +(rs.getDouble("Sale_amount")-rs.getDouble("Paid")-Double.parseDouble(amtpaid.getText()))+"' WHERE Customer_name='"
                    +custname2.getText()+"'";
                  
            if(conn.prepareStatement(sql).executeUpdate()==1){
                SimpleDateFormat dtfrmat=new SimpleDateFormat("yyyy-MM-dd");   
            sql="INSERT INTO Pay ([Date],Customer_name,Payment_mode,Txn_no,Amount_paid) VALUES('"+dtfrmat.format(date1.getDate())+
                    "','"+custname2.getText()+"','"+paymode.getSelectedItem()+"','"+txnno.getText()+"','"+Double.parseDouble(amtpaid.getText())+"')";
           
            pst=conn.prepareStatement(sql);
            pst.executeUpdate();            
            fillpaydetails();
            JOptionPane.showMessageDialog(null, "Paid successfully");
            if(receipt.isSelected()){//JASPER REPORT RECEIPT
                try{
                    HashMap<String, Object> para= new HashMap<>();
                    para.put("custname",custname2.getText());
                    para.put("Company",complab2.getText());
                    para.put("Address",address1.getText());
                    para.put("City", city1.getText());
                    para.put("phone",phone1.getText());
                    para.put("emailid",emailid1.getText());
                    para.put("paymode",paymode.getSelectedItem());
                    String text = (txnno.isVisible())?"TXN: "+txnno.getText():"";
                    para.put("txn",text);
                    para.put("amtpaid",amtpaid.getText());
                    para.put("due",due.getText());
                    para.put("payno",payno.getText());
                    SimpleDateFormat dtfrmat2=new SimpleDateFormat("dd-MM-yyyy");
                    para.put("date", dtfrmat2.format(date1.getDate()));
            
            JasperCompileManager.compileReportToFile("E:\\Data entry_Project\\MyDBmanager\\src\\mydbmanager\\Payment_receipt.jrxml",
                    "E:\\Data entry_Project\\MyDBmanager\\src\\mydbmanager\\Payment_receipt.jasper");
             JasperPrint printing = (JasperPrint) JasperFillManager.fillReport("E:\\Data entry_Project\\MyDBmanager\\src\\mydbmanager\\Payment_receipt.jasper",para);
            JasperViewer.viewReport(printing,false);
            }
            catch(Exception e)
            {
                JOptionPane.showMessageDialog(null, e);
            }
            }
            fillcusttable();
            custname2.setText("");
            address1.setText(" ");
                    city1.setText(" ");
                    phone1.setText(" ");
                    emailid1.setText(" ");
                    complab2.setText(" ");
                    due.setText("0.00");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);
        }
        
    }//GEN-LAST:event_jButton1ActionPerformed

    private void amtpaidFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_amtpaidFocusLost
        if(!amtpaid.getText().equals(""))
        amtpaid.setText(Format(Double.parseDouble(amtpaid.getText())));
    }//GEN-LAST:event_amtpaidFocusLost

    private void paytableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_paytableMouseClicked
        // TODO add your handling code here:
        custname2.setText(paytable.getValueAt(paytable.getSelectedRow(),0).toString());
        fillpaydetails();
    }//GEN-LAST:event_paytableMouseClicked

    private void cashActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cashActionPerformed
        // TODO add your handling code here:
        custname.setText("CASH");
        custname.setEditable(false);
        address.setText("");
        city.setText("");
        phone.setText("");
        emailid.setText("");
    }//GEN-LAST:event_cashActionPerformed

    private void customerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_customerActionPerformed
        // TODO add your handling code here:
        if(custname.getText().equals("CASH")){
        custname.setText("");
        custname.setEditable(true);
        address.setText("");
        city.setText("");
        phone.setText("");
        emailid.setText("");}
    }//GEN-LAST:event_customerActionPerformed

    private void qutpcStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_qutpcStateChanged
        int qpc=Integer.valueOf(qutpc.getValue().toString());
        if(qpc>11)
        {qut.setValue(Integer.parseInt(qut.getValue().toString())+qpc/12);
         qutpc.setValue(qpc-(qpc/12)*12);
        }
        calculate();
    }//GEN-LAST:event_qutpcStateChanged

    private void formWindowGainedFocus(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowGainedFocus
        // TODO add your handling code here:
        retriveIVN();
        allocatestock();
        fillcusttable();
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
            java.util.logging.Logger.getLogger(Sell.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Sell.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Sell.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Sell.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Sell().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField Unit;
    private javax.swing.JButton add;
    private javax.swing.JLabel address;
    private javax.swing.JLabel address1;
    private javax.swing.JTextField amtpaid;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JRadioButton cash;
    private javax.swing.JLabel city;
    private javax.swing.JLabel city1;
    private javax.swing.JButton clear;
    private javax.swing.JLabel complab2;
    private javax.swing.JButton confirm;
    private javax.swing.JTextField custname;
    private javax.swing.JTextField custname2;
    private javax.swing.JRadioButton customer;
    private com.toedter.calendar.JDateChooser date;
    private com.toedter.calendar.JDateChooser date1;
    private javax.swing.JButton delete;
    private javax.swing.JLabel discamt;
    private javax.swing.JTextField discpercent;
    private javax.swing.JLabel due;
    private javax.swing.JLabel emailid;
    private javax.swing.JLabel emailid1;
    private javax.swing.JButton findinvoice;
    private javax.swing.JLabel grandtotal;
    private javax.swing.JTextField itemname;
    private javax.swing.JTable itemtable;
    private javax.swing.JTextField ivno;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JComboBox<String> paymode;
    private javax.swing.JLabel payno;
    private javax.swing.JTable paytable;
    private javax.swing.JLabel phone;
    private javax.swing.JLabel phone1;
    private javax.swing.JButton print;
    private javax.swing.JSpinner qut;
    private javax.swing.JSpinner qutpc;
    private javax.swing.JLabel qutshow;
    private javax.swing.JFormattedTextField rate;
    private javax.swing.JCheckBox receipt;
    private javax.swing.JFormattedTextField total;
    private javax.swing.JLabel totallabel;
    private javax.swing.JTextField txnno;
    // End of variables declaration//GEN-END:variables
}