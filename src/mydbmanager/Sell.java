/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mydbmanager;

import com.toedter.calendar.JTextFieldDateEditor;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.io.File;
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
import javax.security.auth.callback.ConfirmationCallback;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRTableModelDataSource;
import net.sf.jasperreports.swing.JRViewer;
import net.sf.jasperreports.swing.JRViewerToolbar;
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
    HashMap <String, Double> costprice = new HashMap<>();
    HashMap <String, Double> minsellprice = new HashMap<>();
    HashMap <String, Double> mrp = new HashMap<>();
    
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
    ArrayList<JFrame> frames;
    public Sell(Connection con,ArrayList<JFrame> frm) {
//        UIManager.put("TextField[Disabled].backgroundPainter", new FillPainter(new Color(214,217,223)));
        initComponents();
         conn=con;
         frames=frm;
    }
    private void allocatestock(){
        try
        {
            itemnames.clear();
            sellingprice.clear();
            quantityavailable.clear();
            costprice.clear();
            minsellprice.clear();
            mrp.clear();
            String sql="SELECT * FROM Stock";
            pst=conn.prepareStatement(sql);
            rs=pst.executeQuery();
            while(rs.next()){
                itemnames.add(rs.getString("Item"));
                sellingprice.add(rs.getDouble("Selling_price"));
                quantityavailable.put(rs.getString("Item"),rs.getInt("Qut_available"));
                costprice.put(rs.getString("Item"), rs.getDouble("Cost_price"));
                minsellprice.put(rs.getString("Item"),rs.getDouble("Minsell_price"));
                mrp.put(rs.getString("Item"), rs.getDouble("MRP"));
            }
        }
        catch(Exception e){JOptionPane.showMessageDialog(null, e);}
    }
    
    private boolean deductfromstock()
    {
        if(!confirm.isEnabled())
            return false;
        DefaultTableModel tm=(DefaultTableModel)itemtable.getModel();
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
                           return true;//returns true if item not found
                       }
                   }
        return false;
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
                {JOptionPane.showMessageDialog(null, "Record not found");
                 custname.setText("");
                }
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
        public void findprotocol(){
        add.setEnabled(false);
        delete.setEnabled(false);
        confirm.setEnabled(false);
        pen.setEnabled(false);
        print.setSelected(false);
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
        jButton3 = new javax.swing.JButton();
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
        pen = new javax.swing.JLabel();
        returnalert = new javax.swing.JLabel();
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
        jPanel1 = new javax.swing.JPanel();
        jLabel20 = new javax.swing.JLabel();
        sinv = new javax.swing.JTextField();
        jScrollPane4 = new javax.swing.JScrollPane();
        currentinv = new javax.swing.JTable();
        jScrollPane5 = new javax.swing.JScrollPane();
        returntable = new javax.swing.JTable();
        jButton2 = new javax.swing.JButton();
        jLabel21 = new javax.swing.JLabel();
        doz = new javax.swing.JSpinner();
        jTextField3 = new javax.swing.JTextField();
        jLabel22 = new javax.swing.JLabel();
        pcs = new javax.swing.JSpinner();
        jTextField4 = new javax.swing.JTextField();
        returnamt = new javax.swing.JLabel();
        jLabel24 = new javax.swing.JLabel();
        gross = new javax.swing.JLabel();
        jLabel26 = new javax.swing.JLabel();
        disc = new javax.swing.JLabel();
        jLabel28 = new javax.swing.JLabel();
        retdisc = new javax.swing.JLabel();
        jLabel29 = new javax.swing.JLabel();
        returndate = new com.toedter.calendar.JDateChooser();
        returnprint = new javax.swing.JButton();
        stotal = new javax.swing.JLabel();
        jLabel23 = new javax.swing.JLabel();
        returnstotal = new javax.swing.JLabel();
        jLabel27 = new javax.swing.JLabel();
        custnam = new javax.swing.JLabel();

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

        jButton3.setText("jButton3");

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Sales payment and returns");
        setIconImage(ScaleImage.scale("Billosoft.png", 96, 96).getImage());
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
        ((JTextFieldDateEditor)date.getDateEditor()).setEditable(false);

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

        qut.setModel(new javax.swing.SpinnerNumberModel(0, 0, null, 1));
        qut.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                qutStateChanged(evt);
            }
        });

        jLabel8.setText("Total price");

        total.setEditable(false);
        total.setText("0.00");

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

        qutpc.setModel(new javax.swing.SpinnerNumberModel(0, 0, null, 1));
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

        pen.setFont(new java.awt.Font("Segoe UI Emoji", 0, 15)); // NOI18N
        pen.setText("🖊");
        pen.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        pen.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                penMouseClicked(evt);
            }
        });

        returnalert.setFont(new java.awt.Font("Segoe UI Emoji", 0, 11)); // NOI18N
        returnalert.setText("⚠ Some items was returned");
        returnalert.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        returnalert.setVisible(false);
        returnalert.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                returnalertMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(returnalert)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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
                                    .addGroup(jPanel2Layout.createSequentialGroup()
                                        .addComponent(grandtotal)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(pen, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(confirm, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(print, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(clear, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addComponent(jScrollPane2))
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
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 232, Short.MAX_VALUE)
                .addGap(10, 10, 10)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel11)
                    .addComponent(totallabel)
                    .addComponent(returnalert))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jLabel12)
                    .addComponent(discpercent, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(discamt))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel14)
                    .addComponent(grandtotal)
                    .addComponent(pen))
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
        ((JTextFieldDateEditor)date1.getDateEditor()).setEditable(false);

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

        jLabel20.setText("Invoice number");

        sinv.setText("Invoice number here");
        sinv.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                sinvFocusLost(evt);
            }
        });
        sinv.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                sinvMouseClicked(evt);
            }
        });
        sinv.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                sinvKeyPressed(evt);
            }
        });

        currentinv.setModel(new javax.swing.table.DefaultTableModel(
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
        currentinv.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                currentinvMouseClicked(evt);
            }
        });
        jScrollPane4.setViewportView(currentinv);
        if (currentinv.getColumnModel().getColumnCount() > 0) {
            currentinv.getColumnModel().getColumn(0).setPreferredWidth(10);
            currentinv.getColumnModel().getColumn(1).setPreferredWidth(150);
        }

        returntable.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane5.setViewportView(returntable);
        if (returntable.getColumnModel().getColumnCount() > 0) {
            returntable.getColumnModel().getColumn(0).setPreferredWidth(10);
            returntable.getColumnModel().getColumn(1).setPreferredWidth(150);
        }

        jButton2.setText("Return");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jLabel21.setText("Quantity");

        doz.setModel(new javax.swing.SpinnerNumberModel(0, 0, null, 1));
        doz.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                dozStateChanged(evt);
            }
        });

        jTextField3.setText("DOZ");

        jLabel22.setText("-");

        pcs.setModel(new javax.swing.SpinnerNumberModel(0, 0, null, 1));
        pcs.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                pcsStateChanged(evt);
            }
        });

        jTextField4.setText("PCS");

        returnamt.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        returnamt.setText("0.00");

        jLabel24.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel24.setText("Total");

        gross.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        gross.setText("0.00");

        jLabel26.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel26.setText("Total");

        disc.setText("0%");

        jLabel28.setText("Discount");

        retdisc.setText("0%");

        jLabel29.setText("Discount");

        returndate.setDate(new Date());

        returnprint.setText("Print");
        returnprint.setEnabled(false);
        returnprint.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                returnprintActionPerformed(evt);
            }
        });

        stotal.setText("0.00");

        jLabel23.setText("Subtotal");

        returnstotal.setText("0.00");

        jLabel27.setText("Subtotal");

        custnam.setText(" ");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(22, 22, 22)
                        .addComponent(jLabel20)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(sinv, javax.swing.GroupLayout.PREFERRED_SIZE, 134, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(custnam)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(returndate, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(returnprint, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel27)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(returnstotal)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jLabel29)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(retdisc)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jLabel24)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(returnamt)
                                .addGap(61, 61, 61))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(10, 10, 10)
                                .addComponent(jLabel21)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(doz, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel22)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(pcs, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 292, Short.MAX_VALUE)
                                .addComponent(jLabel23)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(stotal)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jLabel28)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(disc)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jLabel26)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(gross)
                                .addGap(72, 72, 72))
                            .addComponent(jScrollPane5, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jScrollPane4))))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel20)
                        .addComponent(sinv, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(custnam))
                    .addComponent(returndate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 242, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel21)
                    .addComponent(doz, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel22)
                    .addComponent(pcs, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(gross)
                    .addComponent(jLabel26)
                    .addComponent(disc)
                    .addComponent(jLabel28)
                    .addComponent(stotal)
                    .addComponent(jLabel23))
                .addGap(7, 7, 7)
                .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 237, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(returnamt)
                    .addComponent(jLabel24)
                    .addComponent(retdisc)
                    .addComponent(jLabel29)
                    .addComponent(jButton2)
                    .addComponent(returnprint)
                    .addComponent(returnstotal)
                    .addComponent(jLabel27))
                .addContainerGap())
        );

        jTabbedPane1.addTab("Return", jPanel1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1)
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

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
        if(Integer.valueOf(qut.getValue().toString())*12+Integer.valueOf(qutpc.getValue().toString())==0)
            return;
         int serialno;
        if(itemtable.getRowCount()==0)
            serialno=1;
        else
            serialno=Integer.parseInt(itemtable.getValueAt(itemtable.getRowCount()-1, 0).toString())+1;
        try{
        if(Integer.valueOf(qut.getValue().toString())*12+Integer.valueOf(qutpc.getValue().toString())<=quantityavailable.get(itemname.getText())){
            if(Double.valueOf(rate.getText())<minsellprice.get(itemname.getText())||Double.valueOf(rate.getText())>mrp.get(itemname.getText())){
                if(JOptionPane.showConfirmDialog(null, "The minimum selling price is "+Format(minsellprice.get(itemname.getText()))+" and the MRP is "+Format(mrp.get(itemname.getText()))+"\nDo you still want to proceed?","Confirmation",ConfirmationCallback.YES_NO_OPTION)!=0)
                    return;}
            for (int i = 0; i < itemtable.getRowCount(); i++) {
                if(itemtable.getValueAt(i, 1).toString().equals(itemname.getText()))
                {    
                    quantityavailable.replace(itemname.getText(), quantityavailable.get(itemname.getText())+quantifier(itemtable.getValueAt(i, 3).toString())-
                            Integer.valueOf(qut.getValue().toString())*12-Integer.valueOf(qutpc.getValue().toString()));
                    itemtable.setValueAt(itemname.getText(), i, 1);
                    itemtable.setValueAt(rate.getText(), i, 2);
                    itemtable.setValueAt(quantifier(Integer.valueOf(qut.getValue().toString())*12+
                Integer.valueOf(qutpc.getValue().toString())), i, 3);
                    itemtable.setValueAt(total.getText(), i, 4);
                    itemname.setText("");
                    total.setText("0.00");
                    rate.setText("0.00");
                    qut.setValue(1);
                    qutpc.setValue(0);
                    qutshow.setText("");
                    summation();
                    return;
                }
            }
        Addrow(new Object[]{serialno,itemname.getText(),rate.getText(),quantifier(Integer.valueOf(qut.getValue().toString())*12+
                Integer.valueOf(qutpc.getValue().toString())),total.getText()});
        quantityavailable.replace(itemname.getText(), quantityavailable.get(itemname.getText())-Integer.valueOf(qut.getValue().toString())*12-
                Integer.valueOf(qutpc.getValue().toString()));
        itemname.setText("");
        total.setText("0.00");
        rate.setText("0.00");
        qut.setValue(1);
        qutpc.setValue(0);
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
            if(!custname.getText().equals("")&&itemtable.getRowCount()!=0) {
                SimpleDateFormat dtfrmat=new SimpleDateFormat("yyyy-MM-dd");
                DefaultTableModel tm = (DefaultTableModel)itemtable.getModel();
                allocatestock();
                if(deductfromstock())
                    return;
                   
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
                            ivno.setText(Format(IVN));
                        for(int i=0;i<itemtable.getRowCount();i++){
                            sql="INSERT INTO Sell (Invoice_no,[Date],Customer_name,Address,City_town,Phone_no,EmailID,Company,Item,Rate,Cost_price,Quantity,Unit,Total,Discount,Gtotal) VALUES ('"+IVN+"','"+
                            dtfrmat.format(date.getDate())+"','"+custname.getText()+"','"+address.getText()+"','"+city.getText()+"','"+phone.getText()+"','"+emailid.getText()+"','"+company+"','"+
                            itemtable.getValueAt(i, 1).toString()+"','"+Double.parseDouble(itemtable.getValueAt(i, 2).toString())+"','"+costprice.get(itemtable.getValueAt(i, 1))*quantifier(itemtable.getValueAt(i, 3).toString())/12d+"','"+
                            quantifier(itemtable.getValueAt(i, 3).toString())+"','"+
                            "PCS"+"','"+            itemtable.getValueAt(i, 4)+"','"+discpercent.getText()+"','"+Double.parseDouble(grandtotal.getText())+"')";
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
                        pen.setEnabled(false);
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
        pen.setEnabled(true);
        returnalert.setVisible(false);
        print.setEnabled(false);
        DefaultTableModel table= (DefaultTableModel)itemtable.getModel();
        table.setRowCount(0);
        allocatestock();
        date.setDate(dt);
    }//GEN-LAST:event_clearActionPerformed
public void findinv()
{
    try {
            boolean gotname=true;
            ResultSet rs2=null;
            returnalert.setVisible(false);
            String sql="SELECT * FROM Sell WHERE Invoice_no='"+Integer.parseInt(ivno.getText())+"'";
            pst=conn.prepareStatement(sql);
            rs2=pst.executeQuery();
            if(!rs2.next()){
            SimpleDateFormat dtfrmat=new SimpleDateFormat("yyyy-MM-dd");
            sql="SELECT * FROM Sell WHERE [Date] = '"+dtfrmat.format(date.getDate())+"' AND Customer_name='"+custname.getText()+"'";
            pst=conn.prepareStatement(sql);
            rs2=pst.executeQuery();
            if(!(rs2.next()))
            {JOptionPane.showMessageDialog(null, "No record found");
            gotname=false;}
            }
            if(gotname){
                int serial=0;
                int lastivns=rs2.getInt("Invoice_no");
                if(conn.prepareStatement("SELECT * FROM Return WHERE Invoice_no='"+lastivns+"'").executeQuery().next())
                    returnalert.setVisible(true);
                ivno.setText(Format(lastivns));
                custname.setText(rs2.getString("Customer_name"));
                address.setText(rs2.getString("Address").equals("null")?" ":rs2.getString("Address"));
                city.setText(rs2.getString("City_town").equals("null")?" ":rs2.getString("City_town"));
                phone.setText(rs2.getString("Phone_no").equals("null")?" ":rs2.getString("Phone_no"));
                emailid.setText(rs2.getString("EmailID").equals("null")?" ":rs2.getString("EmailID"));
                company=rs2.getString("Company").equals("null")?" ":rs2.getString("Company");
                date.setDate(rs2.getDate("Date"));
                print.setEnabled(true);
                discpercent.setText(Double.toString(rs2.getDouble("Discount")));
                ivno.setEnabled(false);
                double gtotal=rs2.getDouble("Gtotal");
                DefaultTableModel table = (DefaultTableModel)itemtable.getModel();
                table.setRowCount(0);
                allocatestock();
                boolean notset=true;
                do 
            {
                if(rs2.getInt("Invoice_no")!=lastivns){
                    lastivns=rs2.getInt("Invoice_no");
                    ivno.setText(ivno.getText()+", "+Format(lastivns));
                    returnalert.setVisible(false);
                    if(notset){
                    print.setEnabled(false);
                    ivno.setEnabled(false);
                    JOptionPane.showMessageDialog(null, "Multiple invoice is shown find one invoice using invoice number to enable "
                            + "printing and see the exact Grand total");
                    notset=false;
                    }
                }
                Object[] row={++serial,rs2.getString("Item"),Format(rs2.getDouble("Rate")),quantifier(rs2.getInt("Quantity")),Format(rs2.getDouble("Total"))};
                Addrow(row);
            }while(rs2.next());
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
            pen.setEnabled(false);
            summation();
            grandtotal.setText(Format(gtotal));
            discamt.setText(Format(gtotal-Double.parseDouble(totallabel.getText())));
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, e);
        }
}
    private void findinvoiceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_findinvoiceActionPerformed
        // TODO add your handling code here:
        findinv();
    }//GEN-LAST:event_findinvoiceActionPerformed
String url;
    private void printActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_printActionPerformed
        // TODO add your handling code here:
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
            try {
                url=new File(Sell.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getPath();
                url=url.substring(0, url.indexOf("Billosoft"));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex);
        }
            para.put("logo",(url+"Billosoft\\print\\logo final.png"));
            para.put("billosoft",(url+"Billosoft\\print\\Billosoft.png"));
            try{
             JasperPrint  printing = (JasperPrint) JasperFillManager.fillReport((url+"Billosoft\\print\\Invoice.jasper")
                     ,para,new JRTableModelDataSource(dtm2));
             JasperViewer jaspervier=new JasperViewer(printing,false);//disappearing the save button due to some problems in font style
             ((javax.swing.JButton)((JRViewerToolbar)((JRViewer)((javax.swing.JPanel)jaspervier.getContentPane().getComponents()[0]).getComponent(0)).getComponent(0)).getComponent(0)).setVisible(false);
             jaspervier.setVisible(true);
            }
            catch(Exception e)
            {
                JOptionPane.showMessageDialog(null, e);
            }
            dtm.setRowCount(rowcount);
    }//GEN-LAST:event_printActionPerformed
    String prevdiscper="";
    private void discpercentKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_discpercentKeyPressed
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
            String amtp=amtpaid.getText();
            amtpaid.setText("");
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
                    para.put("amtpaid",amtp);
                    para.put("due",due.getText());
                    para.put("payno",payno.getText());
                    SimpleDateFormat dtfrmat2=new SimpleDateFormat("dd-MM-yyyy");
                    para.put("date", dtfrmat2.format(date1.getDate()));
                   
                url= new File(Sell.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getPath();
                url=url.substring(0, url.indexOf("Billosoft"));
                    para.put("logo",url+"Billosoft\\print\\logo final.png");
                    para.put("billosoft",url+"Billosoft\\print\\Billosoft.png");
             JasperPrint printing = (JasperPrint) JasperFillManager.fillReport(url+"\\Billosoft\\print\\Payment_receipt.jasper",para);
            JasperViewer jaspervier=new JasperViewer(printing,false);//diappearing the save button due to some font style problems
             ((javax.swing.JButton)((JRViewerToolbar)((JRViewer)((javax.swing.JPanel)jaspervier.getContentPane().getComponents()[0]).getComponent(0)).getComponent(0)).getComponent(0)).setVisible(false);
             jaspervier.setVisible(true);
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
        if(custname.isEnabled()){
        custname.setText("CASH");
        custname.setEditable(false);
        address.setText("");
        city.setText("");
        phone.setText("");
        emailid.setText("");
        company=" ";
        }
    }//GEN-LAST:event_cashActionPerformed

    private void customerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_customerActionPerformed
        // TODO add your handling code here:
        if(custname.getText().equals("CASH")&&custname.isEnabled()){
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
        if(frames.remove(this))
            frames.add(this);
        else
            frames.add(this);
        retriveIVN();
        allocatestock();
        deductfromstock();
        fillcusttable();
    }//GEN-LAST:event_formWindowGainedFocus

    private void penMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_penMouseClicked
        if(!pen.isEnabled())
            return;
        String input=JOptionPane.showInputDialog(this, "Enter the grand total");
        try{
            double gt=Double.parseDouble(input),st=Double.parseDouble(totallabel.getText());
            grandtotal.setText(Format(gt));
            discpercent.setText(Format((st-gt)/st*100));
            discamt.setText(Format(gt-st));
        }catch(Exception e){}
    }//GEN-LAST:event_penMouseClicked
double subtotal = 0d,gtotal= 0d;
boolean retfoundflag=false;
HashMap <String,Double> returncp = new HashMap<>();
    private void sinvKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_sinvKeyPressed
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                if(evt.getKeyCode()==KeyEvent.VK_ENTER)
                {
                    returnitem();
                }
            }
        });
    }//GEN-LAST:event_sinvKeyPressed
    String raddress,rcity,rphone,remailid,rcompany;
    public void returnitem(){
        DefaultTableModel tm = (DefaultTableModel) currentinv.getModel();
        DefaultTableModel tdm= (DefaultTableModel) returntable.getModel();
        try {
                        sinv.setText(Format(Integer.parseInt(sinv.getText())));
                        returnprint.setEnabled(false);
                        String sql="SELECT * FROM Sell WHERE Invoice_no='"+Integer.parseInt(sinv.getText())+"'";
                        pst=conn.prepareStatement(sql);
                        rs=pst.executeQuery();
                        int sr=1;
                        
                        tm.setRowCount(0);
                        disc.setText("0%");
                        gross.setText("0.00");
                        
                        tdm.setRowCount(0);
                        returnamt.setText("0.00");
                        retdisc.setText("0%");
                        subtotal=0d;
                        
                        if(rs.next())
                        {   
                            custnam.setText(rs.getString("Customer_name"));
                            raddress=rs.getString("Address").equals("null")?" ":rs.getString("Address");
                            rcity=rs.getString("City_town").equals("null")?" ":rs.getString("City_town");
                            rphone=rs.getString("Phone_no").equals("null")?" ":rs.getString("Phone_no");
                            remailid= rs.getString("EmailID").equals("null")?" ":rs.getString("EmailID");
                            rcompany=rs.getString("Company").equals("null")?" ":rs.getString("Company");
                            gtotal=rs.getDouble("Gtotal");
                            gross.setText(Format(gtotal));
                            returncp.clear();
                        do{
                        tm.addRow(new Object[]{sr++,rs.getString("Item"),Format(rs.getDouble("Rate")),quantifier(rs.getInt("Quantity")),Format(rs.getDouble("Total"))});
                        returncp.put(rs.getString("Item"),rs.getDouble("Cost_price")/rs.getInt("Quantity"));
                        subtotal+=rs.getDouble("Total");
                        }while(rs.next());
                        stotal.setText(Format(subtotal));
                        disc.setText(Format((subtotal-gtotal)/subtotal*100)+"%");
                        ResultSet rs2 = conn.prepareStatement("SELECT * FROM Return WHERE Invoice_no='"+Integer.parseInt(sinv.getText())+"'").executeQuery();
                        if(rs2.next())
                            {   retfoundflag=true;
                                returnprint.setEnabled(true);
                                returnamt.setText(Format(rs2.getDouble("Gtotal")));
                                retdisc.setText(Double.toString(rs2.getDouble("Discount")));
                                do{
                                    tdm.addRow(new Object[]{"",rs2.getString("Item"),Format(rs2.getDouble("Rate")),quantifier(rs2.getInt("Quantity")),Format(rs2.getDouble("Total"))});
                                }while(rs2.next());
                            summationofreturntable();
                            }
                        else 
                        {    retfoundflag=false;
                            returnprint.setEnabled(false);}
                        }
                        else
                        {
                            
                            JOptionPane.showMessageDialog(null, "Invoice number not found");
                        }
                    } catch (NumberFormatException e) {sinv.setText("");  tm.setRowCount(0);
                        disc.setText("0%");
                        gross.setText("0.00");
                        stotal.setText("0.00");
                        
                        tdm.setRowCount(0);
                        returnamt.setText("0.00");
                        retdisc.setText("0%");
                        returnstotal.setText("0.00");
                        custnam.setText("");}
                      catch (Exception e){
                        JOptionPane.showMessageDialog(null, e);
                    }
    }
    private void sinvMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_sinvMouseClicked
       sinv.setCaretPosition(0);
       sinv.moveCaretPosition(sinv.getText().length());
    }//GEN-LAST:event_sinvMouseClicked
boolean nochangestate=false;
    private void fill()
{
    try{
        if(nochangestate)
            return;
    int selection = currentinv.getSelectedRow();
    String s=(String) currentinv.getValueAt(selection,1);
    if(((int)doz.getValue()*12+(int)pcs.getValue())>quantifier(currentinv.getValueAt(selection, 3).toString()))
    {
        JOptionPane.showMessageDialog(this, "Quantity of return exceeds quantity sold");
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                doz.setValue(0);
                pcs.setValue(0);
            }
        });
    }else{
    boolean found = false;    
    DefaultTableModel tm = (DefaultTableModel) returntable.getModel();
    for(int i=0;i<returntable.getRowCount();i++)
        if(returntable.getValueAt(i,1).equals(s))
        {
            found = true;
            if((int)doz.getValue()*12+(int)pcs.getValue()==0)
            {tm.removeRow(i); break;}
            returntable.setValueAt(quantifier((int)doz.getValue()*12+(int)pcs.getValue()), i, 3);
            returntable.setValueAt(Format(((int)doz.getValue()*12+(int)pcs.getValue())*Double.parseDouble(returntable.getValueAt(i, 2).toString())/12.0d), i, 4);
            break;
        }
    if(!found&&(int)doz.getValue()*12+(int)pcs.getValue()!=0)
    {
        tm.addRow(new Object[]{"",s,currentinv.getValueAt(selection, 2),quantifier((int)doz.getValue()*12+(int)pcs.getValue()),Format((Integer.parseInt(doz.getValue().toString())*12+Integer.parseInt(pcs.getValue().toString()))*Double.parseDouble(currentinv.getValueAt(selection, 2).toString())/12.0d)});
    }
    summationofreturntable();
    }}
    catch(ArrayIndexOutOfBoundsException e){}
}
    private void summationofreturntable()
    {
        double total=0d;
    for(int i=0;i<returntable.getRowCount();i++)
    {   returntable.setValueAt(i+1, i, 0);
        total+=Double.parseDouble(returntable.getValueAt(i, 4).toString());
    }
        returnamt.setText(Format(total*(1-(subtotal-gtotal)/subtotal)));
        returnstotal.setText(Format(total));
        retdisc.setText(Format((subtotal-gtotal)/subtotal*100)+"%");
    }
    private void pcsStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_pcsStateChanged
        int qpc=Integer.valueOf(pcs.getValue().toString());
        if(qpc>11)
        {   
            nochangestate=true;
            doz.setValue(Integer.parseInt(doz.getValue().toString())+qpc/12);
            pcs.setValue(qpc-(qpc/12)*12);
            nochangestate=false;
        } 
        fill();
        returnprint.setEnabled(false);
    }//GEN-LAST:event_pcsStateChanged

    private void dozStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_dozStateChanged
        fill();
        returnprint.setEnabled(false);
    }//GEN-LAST:event_dozStateChanged

    private void currentinvMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_currentinvMouseClicked
        nochangestate=true;
        doz.setValue(0);
        pcs.setValue(0);
        nochangestate=false;
    }//GEN-LAST:event_currentinvMouseClicked

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        try {
            if(!conn.prepareStatement("SELECT * FROM Sell WHERE Invoice_no='"+Integer.parseInt(sinv.getText())+"'").executeQuery().next())
                return;
            SimpleDateFormat dtfrmat=new SimpleDateFormat("yyyy-MM-dd");
            rs=conn.prepareStatement("SELECT * FROM CustomerProfile WHERE Customer_name='"+custnam.getText()+"'").executeQuery();
            if(rs.next())
            {conn.prepareStatement("UPDATE CustomerProfile SET Sale_amount='"+(rs.getDouble("Sale_amount")-Double.parseDouble(returnamt.getText()))+"', Due='"
                                    +(rs.getDouble("Sale_amount")-Double.parseDouble(returnamt.getText())-rs.getDouble("Paid"))+"' WHERE Customer_name='"+custnam.getText()+"'").executeUpdate();
             ResultSet rs2=conn.prepareStatement("SELECT * FROM Return WHERE Invoice_no='"+Integer.parseInt(sinv.getText())+"'").executeQuery();
             if(rs2.next())
             {  retfoundflag=true;
                rs=conn.prepareStatement("SELECT * FROM CustomerProfile WHERE Customer_name='"+custnam.getText()+"'").executeQuery();
                rs.next();
                 conn.prepareStatement("UPDATE CustomerProfile SET Sale_amount='"+(rs.getDouble("Sale_amount")+rs2.getDouble("Gtotal"))+"', Due='"
                                    +(rs.getDouble("Sale_amount")+rs2.getDouble("Gtotal")-rs.getDouble("Paid"))+"' WHERE Customer_name='"+custnam.getText()+"'").executeUpdate();}
            }
            boolean addinstock,shownoallocationdialogue=false;
            if(JOptionPane.showConfirmDialog(this, "Do you want to add the item to the existing stock?", "Confirmation stock addition", ConfirmationCallback.YES_NO_OPTION)==0)
                addinstock=true;
            else
                addinstock=false;
            if(retfoundflag)
            {   ResultSet rs2=conn.prepareStatement("SELECT * FROM Return WHERE Invoice_no='"+Integer.parseInt(sinv.getText())+"'").executeQuery(); 
                if(addinstock)
                {
                    while (rs2.next()) {
                        rs=conn.prepareStatement("SELECT * FROM Stock WHERE Item='"+rs2.getString("Item")+"'").executeQuery();
                        if(rs.next())
                        conn.prepareStatement("UPDATE Stock SET Qut_available='"+(rs.getLong("Qut_available")-rs2.getLong("Quantity"))+"' WHERE Item='"+rs2.getString("Item")+"'").executeUpdate();
                    }
                }
                conn.prepareStatement("DELETE FROM Return WHERE Invoice_no='"+Integer.parseInt(sinv.getText())+"'").executeUpdate();
            }
            for (int i = 0; i < returntable.getRowCount(); i++) {
                if(addinstock)
                {rs=conn.prepareStatement("SELECT * FROM Stock WHERE Item='"+returntable.getValueAt(i, 1)+"'").executeQuery();
                 if(rs.next())
                     conn.prepareStatement("UPDATE Stock SET Qut_available='"+(rs.getLong("Qut_available")+quantifier(returntable.getValueAt(i, 3).toString()))+"' WHERE Item='"+returntable.getValueAt(i, 1)+"'").executeUpdate();
                 else
                     shownoallocationdialogue=true;
                }
                conn.prepareStatement("INSERT INTO Return (Invoice_no,Customer_name,[Date],Item,Rate,Quantity,Unit,Cost_price,Total,Discount,Gtotal) "
                        + "VALUES('"+Integer.parseInt(sinv.getText())+"','"+custnam.getText()+"','"+dtfrmat.format(returndate.getDate())+"','"+returntable.getValueAt(i, 1)+"','"+
                        Double.parseDouble(returntable.getValueAt(i, 2).toString())+"','"+quantifier(returntable.getValueAt(i, 3).toString())+"','PCS','"+
                        (returncp.get((String)returntable.getValueAt(i, 1))*quantifier(returntable.getValueAt(i, 3).toString()))+"','"+
                        returntable.getValueAt(i, 4)+"','"+Double.parseDouble(retdisc.getText().substring(0, retdisc.getText().indexOf('%')))+"','"+
                        Double.parseDouble(returnamt.getText())+"')").executeUpdate();
            }
            JOptionPane.showMessageDialog(this, "Returned successfully"+(addinstock?" and stock allocation complete":""));
            if(shownoallocationdialogue)
            JOptionPane.showMessageDialog(this, "Some items were not allocated as item names were not present in stock");
            returnprint.setEnabled(true);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e);
        }
    }//GEN-LAST:event_jButton2ActionPerformed

    private void sinvFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_sinvFocusLost
        returnitem();
    }//GEN-LAST:event_sinvFocusLost

    private void returnprintActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_returnprintActionPerformed
        Vector<String> columnNames = new Vector<>();
        columnNames.add("No");
        columnNames.add("Item name");
        columnNames.add("Rate");
        columnNames.add("Quantity");
        columnNames.add("Total");       
        DefaultTableModel dtm= (DefaultTableModel) returntable.getModel();
        int rowcount=dtm.getRowCount();
        DefaultTableModel dtm2= new DefaultTableModel(dtm.getDataVector(),columnNames);
        for(int i=dtm2.getRowCount();i<(int)(Math.ceil(rowcount/18.0d))*18;i++)
        dtm2.addRow(new Object[]{"","","","",""});
            HashMap<String, Object> para= new HashMap<>();
            para.put("Grandtotal",returnamt.getText());
            para.put("Subtotal",returnstotal.getText());
            para.put("discountper",retdisc.getText().substring(0, retdisc.getText().indexOf('%')));
            para.put("discountamt",Format(Double.parseDouble(returnstotal.getText())-Double.parseDouble(returnamt.getText())));
            para.put("custname",custnam.getText());
            para.put("Company",rcompany);
            para.put("Address",raddress);
            para.put("City", rcity);
            para.put("phone",rphone);
            para.put("emailid",remailid);
            para.put("Invoice_no", Format(Integer.parseInt(sinv.getText())));
            SimpleDateFormat dtfrmat=new SimpleDateFormat("dd-MM-yyyy");
            para.put("Date", dtfrmat.format(returndate.getDate()));
            try {
                url=new File(Sell.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getPath();
                url=url.substring(0, url.indexOf("Billosoft"));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex);
        }
            para.put("logo",(url+"Billosoft\\print\\logo final.png"));
            para.put("billosoft",(url+"Billosoft\\print\\Billosoft.png"));
            try{
             JasperPrint  printing = (JasperPrint) JasperFillManager.fillReport((url+"Billosoft\\print\\Return_receipt.jasper")
                     ,para,new JRTableModelDataSource(dtm2));
             JasperViewer jaspervier=new JasperViewer(printing,false);//disappearing the save buton due to some font style problems
             ((javax.swing.JButton)((JRViewerToolbar)((JRViewer)((javax.swing.JPanel)jaspervier.getContentPane().getComponents()[0]).getComponent(0)).getComponent(0)).getComponent(0)).setVisible(false);
             jaspervier.setVisible(true);
            }
            catch(Exception e)
            {
                JOptionPane.showMessageDialog(null, e);
            }
            dtm.setRowCount(rowcount);
    }//GEN-LAST:event_returnprintActionPerformed

    private void returnalertMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_returnalertMouseClicked
        sinv.setText(ivno.getText());
        returnitem();
        jTabbedPane1.setSelectedIndex(2);
    }//GEN-LAST:event_returnalertMouseClicked

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
    private javax.swing.JTable currentinv;
    private javax.swing.JLabel custnam;
    public javax.swing.JTextField custname;
    public javax.swing.JTextField custname2;
    public javax.swing.JRadioButton customer;
    private com.toedter.calendar.JDateChooser date;
    private com.toedter.calendar.JDateChooser date1;
    private javax.swing.JButton delete;
    private javax.swing.JLabel disc;
    private javax.swing.JLabel discamt;
    private javax.swing.JTextField discpercent;
    private javax.swing.JSpinner doz;
    private javax.swing.JLabel due;
    private javax.swing.JLabel emailid;
    private javax.swing.JLabel emailid1;
    private javax.swing.JButton findinvoice;
    private javax.swing.JLabel grandtotal;
    private javax.swing.JLabel gross;
    private javax.swing.JTextField itemname;
    private javax.swing.JTable itemtable;
    public javax.swing.JTextField ivno;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
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
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    public javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField3;
    private javax.swing.JTextField jTextField4;
    private javax.swing.JComboBox<String> paymode;
    private javax.swing.JLabel payno;
    private javax.swing.JTable paytable;
    private javax.swing.JSpinner pcs;
    private javax.swing.JLabel pen;
    private javax.swing.JLabel phone;
    private javax.swing.JLabel phone1;
    private javax.swing.JButton print;
    private javax.swing.JSpinner qut;
    private javax.swing.JSpinner qutpc;
    private javax.swing.JLabel qutshow;
    private javax.swing.JFormattedTextField rate;
    private javax.swing.JCheckBox receipt;
    private javax.swing.JLabel retdisc;
    private javax.swing.JLabel returnalert;
    private javax.swing.JLabel returnamt;
    private com.toedter.calendar.JDateChooser returndate;
    private javax.swing.JButton returnprint;
    private javax.swing.JLabel returnstotal;
    private javax.swing.JTable returntable;
    public javax.swing.JTextField sinv;
    private javax.swing.JLabel stotal;
    private javax.swing.JFormattedTextField total;
    private javax.swing.JLabel totallabel;
    private javax.swing.JTextField txnno;
    // End of variables declaration//GEN-END:variables
}