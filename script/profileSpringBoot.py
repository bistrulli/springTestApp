import subprocess
import psutil as ps
import numpy as np
import scipy as so
from pathlib import Path
import requests as reg
import time
import threading
import matplotlib.pyplot as plt

toStop=False

def startService():
    scriptPath=Path(__file__)
    svcPath=scriptPath.parent.parent.joinpath("target").joinpath("k8testpod-0.0.1.jar")
    if(not svcPath.exists()):
        raise ValueError("service file does not exist")
    springProc=subprocess.Popen(["java","-jar",str(svcPath)],stdout=subprocess.DEVNULL)
    
    while(True):
        try:
            reg.get("http://localhost")
            print("service started")
            break
        except Exception as e:
            print("waiting service to start...")
            time.sleep(1)
    
    return springProc
    

def profileMemService(svcProc):
    mem_info = svcProc.memory_info()
    print(f"Memory RSS (Resident Set Size): {mem_info.rss / (1024 * 1024):.2f} MB")
    return mem_info.rss / (1024 * 1024)
    
def stressSvc():
    global toStop
    user = threading.Thread(target=httpUsers)
    user.start()

def httpUsers():
    while(not toStop):
        try:
            reg.get("http://localhost")
        except:
            pass

def stopService(svc):
    svc.terminate()
    svc.kill()
    outs, errs = svc.communicate()

if __name__ == '__main__':
    svc=startService()
    psutilsvc=ps.Process(svc.pid)
    it=0
    users=[]
    memory=[]
    while(True):
        memory+=[profileMemService(psutilsvc)]
        if(it>30):
            toStop=True
            break
        else:
            it+=1
            users+=[threading.Thread(target=httpUsers)]
            users[-1].start()
            time.sleep(1)
    stopService(svc)
    
    plt.figure()
    plt.plot(memory)
    plt.show()