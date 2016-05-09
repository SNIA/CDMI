import sys, traceback
from PyQt4.QtCore import *
from PyQt4.QtGui import *

from os import path

import json
import urllib2
import ssl

imgStr = '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'

ssl._create_default_https_context = ssl._create_unverified_context

data = []
endpoints = ["http://cdmi-indigo.recas.ba.infn.it","https://cdmi-qos.data.kit.edu"]

class MainWindow(QMainWindow):
    def __init__(self):
        super(MainWindow, self).__init__()
        
        self.initUI()
        
    def initUI(self):               
        
        self.statusBar().showMessage('Ready')
        
        self.setGeometry(300, 300, 800, 800)
        self.setWindowTitle('CDMI FileBrowser')
        self.browser = Browser(self.statusBar)
        self.setCentralWidget(self.browser)

        qr = self.frameGeometry()
        cp = QDesktopWidget().availableGeometry().center()
        qr.moveCenter(cp)
        self.move(qr.topLeft())
 
        self.show()

class Browser(QWidget):
    def __init__(self, statusBar):
    
        QWidget.__init__(self)
        
        self.statusBar = statusBar
        
        self.lineEdit = QLineEdit()

        self.connectBtn = QPushButton('Connect', self)
        self.connectBtn.clicked.connect(self.list_cdmi)
        
        self.treeView = QTreeView()
        self.treeView.setContextMenuPolicy(Qt.CustomContextMenu)
        #self.treeView.customContextMenuRequested.connect(self.openMenu)
        
        self.model = QStandardItemModel()
        self.addItems(self.model, data)
        self.treeView.setModel(self.model)
        self.model.setHorizontalHeaderLabels([self.tr("Objects")])
        
        self.treeView.clicked.connect(self.on_treeView_clicked)

        self.metaView = QTextEdit()
        self.metaView.setReadOnly(True)
        #self.metaView.setContextMenuPolicy(Qt.CustomContextMenu)
        #self.metaView.customContextMenuRequested.connect(self.openMenu)
        #self.metaModel = QStandardItemModel()
        #self.addItems(self.metaModel, [])
        #self.metaModel.setHorizontalHeaderLabels([self.tr("Metadata")])
        #self.metaView.setModel(self.metaModel)
        #self.logo = QImage("logo.png")
        self.logo = QImage()
        self.logo.loadFromData(QByteArray.fromBase64(imgStr))
        self.logo = self.logo.scaled(400,400, aspectRatioMode=Qt.KeepAspectRatio, transformMode=Qt.SmoothTransformation)
        
        self.logoFrame = QLabel()
        self.logoFrame.setPixmap(QPixmap.fromImage(self.logo))
        
        text = "Available endpoints:\n\n"
        for endpoint in endpoints:
            text += endpoint + ' \n'
        
        self.textEdit = QTextEdit()
        self.textEdit.setReadOnly(True)
        self.textEdit.setPlainText(text)
        
        layout = QGridLayout()
        layout.setSpacing(10)
        
        layout.addWidget(self.treeView, 1, 0)
        layout.addWidget(self.metaView, 1, 1)
        layout.addWidget(self.lineEdit, 3, 0)
        layout.addWidget(self.connectBtn, 3,1)
        layout.addWidget(self.textEdit, 2, 0)
        layout.addWidget(self.logoFrame, 2, 1)

        self.setLayout(layout)

    @pyqtSlot(QModelIndex)
    def on_treeView_clicked(self, index):
        indexItem = self.model.index(index.row(), 0, index.parent())
        fullPath = '/'

        isRoot = False
        i = index.parent()
        while not isRoot:
            parentItem = self.model.index(i.row(), 0, i.parent())
            p = self.model.data(parentItem)
            if p.toPyObject() is None:
                isRoot = True
            else:
                fullPath = path.join(fullPath, str(p.toPyObject()))
                i = i.parent()

        item = self.model.data(indexItem)
        fullPath = path.join(fullPath, str(item.toPyObject()))

        metaData = self.get_object(fullPath)

        self.metaView.setPlainText(metaData)


    def list_children(self, root):
    #print "DEBUG: open {}".format(root)
        root = root.strip()
        f = urllib2.urlopen(root)
        j = f.read()
        res = json.loads(j)

        objectName = res.get('objectName')
        children = res.get('children')
  
        if children is None:
            return [(objectName, [])]
    
        return [(objectName, self.list_children(root+"/"+child)) for child in children if not "cdmi" in child]
        
    def get_object(self, path):
        url  = self.lineEdit.text()
        try:
            url = str(url).strip()+path
            log = "Connect to {}".format(url)
            self.statusBar().showMessage(log)

            f = urllib2.urlopen(url)
            j = f.read()
            res = json.loads(j)

            data = json.dumps(res, sort_keys=True,indent=4, separators=(',', ': '))
            return data
        except:
            self.statusBar().showMessage("ERROR")
            print "Exception in user code:"
            print '-'*60
            traceback.print_exc(file=sys.stdout)
            print '-'*60

    def list_cdmi(self):
        url = self.lineEdit.text()
        try:
            url = str(url)
            log = "Connect to {}".format(url)

            self.statusBar().showMessage(log)
            data = self.list_children(url)
            self.statusBar().showMessage(log)
            
            self.model = QStandardItemModel()
            self.addItems(self.model, data)
            self.model.setHorizontalHeaderLabels([self.tr("Objects")])
            
            self.treeView.setModel(None)
            self.treeView.setModel(self.model)
        except:
            self.statusBar().showMessage("ERROR")
            print "Exception in user code:"
            print '-'*60
            traceback.print_exc(file=sys.stdout)
            print '-'*60
        
    def addItems(self, parent, elements):
    
        for text, children in elements:
            item = QStandardItem(text)
            parent.appendRow(item)
            if children:
                self.addItems(item, children)
    
    def openMenu(self, position):
    
        indexes = self.treeView.selectedIndexes()
        if len(indexes) > 0:
        
            level = 0
            index = indexes[0]
            while index.parent().isValid():
                index = index.parent()
                level += 1
        
        menu = QMenu()
        if level == 0:
            menu.addAction(self.tr("Show root container metadata"))
        elif level == 1:
            menu.addAction(self.tr("Show container metadata"))
        elif level == 2:
            menu.addAction(self.tr("Show object metadata"))
        
        menu.exec_(self.treeView.viewport().mapToGlobal(position))


if __name__ == "__main__":

    app = QApplication(sys.argv)
    MainWindow = MainWindow()
    MainWindow.show()
    sys.exit(app.exec_())
