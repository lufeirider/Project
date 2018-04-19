import psutil
import threading
from threading import Timer
import time
import yara
import os

#匹配规则
yararule = ""

#获取该目录下所有的规则文件
def getRules(path):
    filepath = {}
    for index, file in enumerate(os.listdir(path)):
        rupath = os.path.join(path, file)
        key = "rule" + str(index)
        filepath[key] = rupath
    yararule = yara.compile(filepaths=filepath)
    return yararule

#使用yara对进程内存进行匹配规则
def checkProcess(pid):
    matches = yararule.match(pid=pid)
    if len(matches) > 0:
        print(matches)
    # try:
        # matches = yararule.match(pid=pid)
        # if len(matches) > 0:
            # print(matches)
    # except:
        # pass

#获取新进程，与暂停三秒之后的进程列表对比，获取新的进程
def getNewProcess():
    global oldProcessList
    print("new thread check")
    newProcessList = psutil.pids()
    tmp = [b for b in newProcessList if b not in oldProcessList]
    print(tmp)
    time.sleep(1)
    for i in tmp:
        print("check pid=" + str(i))
        checkProcess(i)
    oldProcessList = newProcessList

#每隔三秒就获取新的进程
def runTimer():
    while True:
        global oldProcessList
        oldProcessList = psutil.pids()
        t = Timer(3, getNewProcess)
        t.start()
        t.join()


if __name__ == '__main__':
    oldProcessList = []
    rulepath = "./rules"
    yararule = getRules(rulepath)

    t = threading.Thread(target=runTimer)
    t.start()
    t.join()
    print('main_end')