
package procesos;


import java.util.Comparator;
import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.RowFilter;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
/**
 *
 * @author alcan
 */
public class administrador_de_tareas extends javax.swing.JFrame {
    
    private DefaultTableModel modelo;
    private TableRowSorter<TableModel> sorter;
   

    public administrador_de_tareas() {
        initComponents();
        getContentPane().setBackground(Color.WHITE);
        this.setLocationRelativeTo(null);
        No_procesos.setFocusable(false);
 
        mostrar_procesos();
 
        // Configuración del sorter
        configurarSorter();
 
        // Configuración del buscador
        jTextField1.getDocument().addDocumentListener(new DocumentListener() {
 
            @Override
            public void insertUpdate(DocumentEvent e) {
                filtrar();
            }
 
            @Override
            public void removeUpdate(DocumentEvent e) {
                filtrar();
            }
 
            @Override
            public void changedUpdate(DocumentEvent e) {
                filtrar();
            }
 
            private void filtrar() {
 
                String texto = jTextField1.getText();
 
                if (texto.trim().isEmpty()) {
 
                    sorter.setRowFilter(null);
 
                } else {
 
                    sorter.setRowFilter(
                        RowFilter.regexFilter("(?i)" + texto, 0)
                    );
                }
            }
        });
    }

    private void configurarSorter() {
 
        sorter = new TableRowSorter<>(jtabla_datos.getModel());
 
        // Comparador para la columna "Uso de memoria"
        // Se dejan solo los dígitos, así funciona con
        // "39,464 K", "39.464 KB" o "39 464 K"
        sorter.setComparator(4, new Comparator<Object>() {
 
            @Override
            public int compare(Object o1, Object o2) {
 
                String valor1 = o1.toString().replaceAll("[^0-9]", "");
                String valor2 = o2.toString().replaceAll("[^0-9]", "");
 
                if (valor1.isEmpty() || valor2.isEmpty()) {
                    return 0;
                }
 
                return Long.compare(
                    Long.parseLong(valor1),
                    Long.parseLong(valor2)
                );
            }
        });
 
        // VINCULACIÓN CLAVE: vuelve a asignar el sorter actualizado a la tabla
        jtabla_datos.setRowSorter(sorter);
    }
 
    private void Alineacion_Columnas() {
 
        DefaultTableCellRenderer Alinear =
                new DefaultTableCellRenderer();
 
        Alinear.setHorizontalAlignment(SwingConstants.RIGHT);
 
        jtabla_datos.getColumnModel().getColumn(1)
                .setCellRenderer(Alinear);
 
        jtabla_datos.getColumnModel().getColumn(2)
                .setCellRenderer(Alinear);
 
        jtabla_datos.getColumnModel().getColumn(3)
                .setCellRenderer(Alinear);
 
        jtabla_datos.getColumnModel().getColumn(4)
                .setCellRenderer(Alinear);
    }
    
    void LimpiarTabla() {
 
        jtabla_datos.setModel(
            new javax.swing.table.DefaultTableModel(
                new Object[][] {
 
                },
                new String[] {
                    "Nombre",
                    "PID",
                    "Tipo de sesión ",
                    "Número de sesión",
                    "Uso de memoria"
                }
            ) {
 
                boolean[] canEdit = new boolean[] {
                    false,
                    false,
                    false,
                    false,
                    false
                };
 
                public boolean isCellEditable(
                        int rowIndex,
                        int columnIndex) {
 
                    return canEdit[columnIndex];
                }
            }
        );
 
        // Volver a configurar el sorter
        configurarSorter();
    }
    
    private void mostrar_procesos() {
 
        int ICol = 0, ICont = 0;
 
        modelo = (DefaultTableModel) jtabla_datos.getModel();
 
        Object[] Fila = new Object[5];
 
        int i = 0;
 
        String StrAuxi = "";
 
        try {
 
            String line;
 
            Process p = Runtime.getRuntime().exec(
                System.getenv("windir")
                + "\\system32\\"
                + "tasklist.exe"
            );
 
            BufferedReader input = new BufferedReader(
                new InputStreamReader(p.getInputStream())
            );
 
            while ((line = input.readLine()) != null) {
 
                if (i >= 4) {
 
                    ICont = 0;
 
                    while (ICont <= 4) {
 
                        String[] sep = line.split("\\s+");
 
                        if (ICont != 4) {
 
                            Fila[ICont] = sep[ICont];
 
                        } else {
 
                            Fila[ICont] =
                                    sep[ICont]
                                    + " "
                                    + sep[ICont + 1];
                        }
 
                        ICont++;
                    }
 
                    modelo.addRow(Fila);
                }
 
                i++;
            }
 
            input.close();
 
            Alineacion_Columnas();
 
            No_procesos.setText(String.valueOf(i));
 
        } catch (Exception err) {
 
            err.printStackTrace();
        }
    }
    
    
    public void Matar_proceso() {
 
        int filaSeleccionada = jtabla_datos.getSelectedRow();

    // 1. Validar que se haya seleccionado una fila
    if (filaSeleccionada == -1) {
        JOptionPane.showMessageDialog(
            null,
            "ERROR, No se ha seleccionado ningún proceso",
            "Error",
            JOptionPane.WARNING_MESSAGE
        );
        return;
    }

    // 2. Obtener el valor de la celda de forma segura
    Object valorCelda = jtabla_datos.getValueAt(filaSeleccionada, 0);

    // 3. Validar que la celda no esté vacía
    if (valorCelda == null || valorCelda.toString().trim().isEmpty()) {
        JOptionPane.showMessageDialog(
            null,
            "ERROR, El nombre del proceso está vacío",
            "Error",
            JOptionPane.WARNING_MESSAGE
        );
        return;
    }

    String StrCelda = valorCelda.toString().trim();

    try {
        Process hijo = Runtime.getRuntime().exec("taskkill /F /IM " + StrCelda);
        hijo.waitFor();
    } catch (IOException | InterruptedException ex) {
        Logger.getLogger(administrador_de_tareas.class.getName())
              .log(Level.SEVERE, null, ex);
    }
    }
    
    
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jtabla_datos = new javax.swing.JTable();
        jLabel2 = new javax.swing.JLabel();
        No_procesos = new javax.swing.JTextField();
        Actualizar = new javax.swing.JButton();
        ORDENAR = new javax.swing.JButton();
        jTextField1 = new javax.swing.JTextField();
        jterminar_procesos = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jtabla_datos.setBackground(new java.awt.Color(204, 204, 255));
        jtabla_datos.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
        jtabla_datos.setFont(new java.awt.Font("Dubai", 1, 18)); // NOI18N
        jtabla_datos.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Nombre", "PID", "Tipo de sesión ", "Número de sesión", "Uso de memoria"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(jtabla_datos);

        jLabel2.setText("TOTAL DE PROCESOS: ");

        No_procesos.addActionListener(this::No_procesosActionPerformed);

        Actualizar.setText("ACTUALIZAR");
        Actualizar.addActionListener(this::ActualizarActionPerformed);

        ORDENAR.setText("ORDENAR");
        ORDENAR.addActionListener(this::ORDENARActionPerformed);

        jTextField1.setText("BUSQUEDA");
        jTextField1.setToolTipText("j");
        jTextField1.addActionListener(this::jTextField1ActionPerformed);

        jterminar_procesos.setText("MATAR PROCESO");
        jterminar_procesos.addActionListener(this::jterminar_procesosActionPerformed);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(No_procesos, javax.swing.GroupLayout.PREFERRED_SIZE, 167, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(Actualizar, javax.swing.GroupLayout.PREFERRED_SIZE, 223, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(ORDENAR, javax.swing.GroupLayout.PREFERRED_SIZE, 222, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 207, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jterminar_procesos, javax.swing.GroupLayout.PREFERRED_SIZE, 248, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 1277, Short.MAX_VALUE)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(0, 632, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(No_procesos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jterminar_procesos)
                    .addComponent(Actualizar)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(ORDENAR)))
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 616, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(33, Short.MAX_VALUE)))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void No_procesosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_No_procesosActionPerformed

    }//GEN-LAST:event_No_procesosActionPerformed

    private void ActualizarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ActualizarActionPerformed
        LimpiarTabla();
        mostrar_procesos();
    }//GEN-LAST:event_ActualizarActionPerformed

    private void ORDENARActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ORDENARActionPerformed
String[] opciones = {
            "Nombre: A - Z",
            "Nombre: Z - A",
           
        };

        int opcion = JOptionPane.showOptionDialog(
            this,
            "Seleccione el tipo de ordenamiento:",
            "Ordenar procesos",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            opciones,
            opciones[0]
        );

        if (opcion == 0) {

            // Nombre A - Z
            sorter.setSortKeys(
                java.util.Arrays.asList(
                    new javax.swing.RowSorter.SortKey(
                        0,
                        javax.swing.SortOrder.ASCENDING
                    )
                )
            );

        } else if (opcion == 1) {

            // Nombre Z - A
            sorter.setSortKeys(
                java.util.Arrays.asList(
                    new javax.swing.RowSorter.SortKey(
                        0,
                        javax.swing.SortOrder.DESCENDING
                    )
                )
            );

        } else if (opcion == 2) {

            // Memoria: Mayor - Menor
            sorter.setSortKeys(
                java.util.Arrays.asList(
                    new javax.swing.RowSorter.SortKey(
                        4,
                        javax.swing.SortOrder.DESCENDING
                    )
                )
            );

        } else if (opcion == 3) {

            // Memoria: Menor - Mayor
            sorter.setSortKeys(
                java.util.Arrays.asList(
                    new javax.swing.RowSorter.SortKey(
                        4,
                        javax.swing.SortOrder.ASCENDING
                    )
                )
            );
        }
    }//GEN-LAST:event_ORDENARActionPerformed

    private void jTextField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField1ActionPerformed

    }//GEN-LAST:event_jTextField1ActionPerformed

    private void jterminar_procesosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jterminar_procesosActionPerformed

        
        Matar_proceso();
        LimpiarTabla();
        mostrar_procesos();
        
    }//GEN-LAST:event_jterminar_procesosActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
       try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(administrador_de_tareas.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(administrador_de_tareas.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(administrador_de_tareas.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(administrador_de_tareas.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
 
        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new administrador_de_tareas().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton Actualizar;
    private javax.swing.JTextField No_procesos;
    private javax.swing.JButton ORDENAR;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTable jtabla_datos;
    private javax.swing.JButton jterminar_procesos;
    // End of variables declaration//GEN-END:variables
}
