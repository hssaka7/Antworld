package antworld.renderer;


import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
//import java.awt.Font;
import java.awt.Insets;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import antworld.common.GameObject;
import antworld.common.TeamNameEnum;
import antworld.server.AntWorld;
import antworld.server.Nest;
import antworld.server.Nest.NestStatus;

@SuppressWarnings("serial")
public class DataViewer extends JFrame
{
  
  //private Font myFont = new Font ("SansSerif", Font.PLAIN , 16);
  public static JTable table_nestList, table_FoodList;
  
  public DataViewer(ArrayList<Nest> nestList)
  {
    this.setTitle(AntWorld.title);

    this.setBounds(0, 0, 768, 500);
    this.setVisible(true);
    this.setResizable(true);
    this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
    Container contentPane = this.getContentPane();
    // contentPane.setLayout(null);
    JTabbedPane tabbedPane = new JTabbedPane();
    contentPane.add(tabbedPane);

    JPanel panel_NestList = new JPanel();
    
    tabbedPane.addTab("Nest List", panel_NestList);
    int outsideWidth = tabbedPane.getWidth();
    int outsideHeight = tabbedPane.getHeight();
    Insets insets = tabbedPane.getInsets();
    int panelWidth = outsideWidth - insets.left - insets.right;
    int panelHeight = outsideHeight - insets.top - insets.bottom;
    
    
    String[] columnNames =  new String[7];
    columnNames[0] = "Nest";
    columnNames[1] = "Team";
    columnNames[2] = "Status";
    columnNames[3] = "Ant Count";
    columnNames[4] = "Food Count";
    columnNames[5] = "Water Count";
    columnNames[6] = "Score";

   
    DefaultTableModel model_table_nestList = new DefaultTableModel(null,columnNames);  
    
    
    table_nestList = new JTable(model_table_nestList);

 
    table_nestList.setPreferredScrollableViewportSize(new Dimension(panelWidth, panelHeight));
    
    JScrollPane scrollPane = new JScrollPane(table_nestList);
    
    table_nestList.setFillsViewportHeight(true);
    panel_NestList.setLayout(new BorderLayout());
    panel_NestList.add(scrollPane);
    
   
    model_table_nestList.setRowCount(nestList.size());
    table_nestList.selectAll();
    //tabbedPane.setSelectedIndex(0);
    

    
    
    //////////////=============== Ant List Table ============================
    
    JPanel panel_Details = new JPanel();
    tabbedPane.addTab("Ant List", panel_Details);
    
//    String[] columnAntList =  new String[5+FoodType.SIZE-1];
//    columnAntList[0] = "Nest";
//    columnAntList[1] = "Team";
//    columnAntList[2] = "Center X";
//    columnAntList[3] = "Center Y";
//    columnAntList[4] = "Ant Count";
//    int i = 5;
//    for (FoodType type : FoodType.values())
//    { if (type != FoodType.UNKNOWN)
//      { columnNames[i] = type.name();
//        i++;
//      }
//    }
//   
//    DefaultTableModel model_table_nestList = new DefaultTableModel(null,columnNames);  
//    
//    
//    table_nestList = new JTable(model_table_nestList);
//
// 
//    table_nestList.setPreferredScrollableViewportSize(new Dimension(panelWidth, panelHeight));
//    
//    JScrollPane scrollPane = new JScrollPane(table_nestList);
//    
//    table_nestList.setFillsViewportHeight(true);
//    panel_NestList.setLayout(new BorderLayout());
//    panel_NestList.add(scrollPane);
//    
//
//    JPanel panel_Details = new JPanel();
//    tabbedPane.addTab("Ant List", panel_Details);
//    
//   
//    model_table_nestList.setRowCount(nestList.size());
//    table_nestList.selectAll();
    
    //////////////=============== Food In World ============================
    
    JPanel panel_Food = new JPanel();
    tabbedPane.addTab("Food List", panel_Food);
  
  }

  public void update(ArrayList<Nest> nestList)
  {
    for (int row = 0; row < nestList.size(); row++)
    {
      Nest nest = nestList.get(row);

      table_nestList.setValueAt(nest.nestName, row, 0);
      table_nestList.setValueAt(nest.team, row, 1);

      String status = "OK";
      if (nest.getStatus() == NestStatus.DISCONNECTED) status = "???";
      else if (nest.getStatus() == NestStatus.UNDERGROUND) status = "---";

      int score = nest.calculateScore();
      table_nestList.setValueAt(status, row, 2);
      table_nestList.setValueAt(nest.getResourceCount(GameObject.GameObjectType.ANT), row, 3);
      table_nestList.setValueAt(nest.getResourceCount(GameObject.GameObjectType.FOOD), row, 4);
      table_nestList.setValueAt(nest.getResourceCount(GameObject.GameObjectType.WATER), row, 5);
      table_nestList.setValueAt(score, row, 6);

      // for (int x=0; x<columnNames.length; x++)
      // {
      // table_nestList.setValueAt(nestData[x][y], y, x);
      // table_nestList.getCellRenderer(y,
      // x).getTableCellRendererComponent(table_nestList, nestData[x][y], true,
      // true, y, x).setFont(myFont);
      // }
    }
  }

}
