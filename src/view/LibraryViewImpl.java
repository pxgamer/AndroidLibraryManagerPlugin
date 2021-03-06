package view;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import entity.ListItem;
import org.jetbrains.annotations.Nullable;
import presenter.LibraryPresenterImpl;
import widget.MyCheckBoxList;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

/**
 * Created by pkhope on 2016/5/19.
 */
public class LibraryViewImpl extends DialogWrapper implements LibraryView {

    Project mProject;
    private JPanel mMainPanel;
    private JComboBox moduleCombo;
    private DefaultListModel mListModel;
    private MyCheckBoxList mList;
    private JPanel mPanel;
    private JButton mAdd;
    private JButton mDelete;

    private LibraryPresenterImpl mPresenter;

    public LibraryViewImpl(@Nullable Project project) {
        super(project);

        mProject = project;

        mPresenter = new LibraryPresenterImpl(this,project);

        setTitle("Dependency Manager");
        initModuleCombo();
        initLibraryList();

        mPresenter.load();
        mPresenter.changeModule((String) moduleCombo.getItemAt(0));

        mAdd.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String libraryName = Messages.showInputDialog(mProject, "Input your dependency", "Add", null);
                if (libraryName == null){
                    return;
                }
                mListModel.addElement(new JCheckBox(libraryName));
                mPresenter.addItem(libraryName);
            }
        });

        mDelete.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int index = mList.getSelectedIndex();
                mListModel.remove(index);
                mPresenter.removeItem(index);
            }
        });

        init();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return mMainPanel;
    }

    protected void initModuleCombo(){
        ModuleManager moduleManager = ModuleManager.getInstance(mProject);
        Module[] modules = moduleManager.getModules();
        for (Module module : modules){
            if (!module.getName().equals(mProject.getName())){
                moduleCombo.addItem(module.getName());
            }
        }


        moduleCombo.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                mListModel.clear();
                mPresenter.changeModule((String) e.getItem());
            }
        });
    }

    protected void initLibraryList(){

        mListModel = new DefaultListModel();
        mList = new MyCheckBoxList();
        mList.setModel(mListModel);
        mPanel.setLayout(new BoxLayout(mPanel,BoxLayout.Y_AXIS));
        mPanel.add(mList);
        mList.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        mList.addMouseListener(new MouseAdapter()
                         {
                             public void mousePressed(MouseEvent e)
                             {
                                 int index = mList.locationToIndex(e.getPoint());

                                 if (index != -1) {
                                     JCheckBox checkbox = (JCheckBox)
                                             mList.getModel().getElementAt(index);
                                     checkbox.setSelected(
                                             !checkbox.isSelected());
                                     repaint();

                                     mPresenter.setSelected(index,checkbox.isSelected());
                                 }
                             }
                         }
        );
    }

    public void saveData(){
        mPresenter.save();
    }

    @Override
    public void update(List<ListItem> list) {

        JCheckBox checkBox = null;
        if (list.size() != mListModel.size()){
            for (ListItem item : list){
                checkBox = new JCheckBox(item.getName());
                checkBox.setSelected(item.getSelected());
                mListModel.addElement(checkBox);
            }
        }else{
            int index = 0;
            for (ListItem item : list) {
                checkBox = (JCheckBox)mListModel.getElementAt(index++);
                checkBox.setSelected(item.getSelected());
            }
            mList.repaint();
        }
    }
}
