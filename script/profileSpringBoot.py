import subprocess
import psutil as ps
import numpy as np
import scipy as so
from pathlib import Path
import requests as reg
import time
import threading
from  threading import Lock
import matplotlib.pyplot as plt
import traceback 


toStop=False
nrbreq=0
nrbLock=Lock()

def startService():
    scriptPath=Path(__file__)
    svcPath=scriptPath.parent.parent.joinpath("target").joinpath("k8testpod-0.0.1.jar")
    if(not svcPath.exists()):
        raise ValueError("service file does not exist")
    springProc=subprocess.Popen(["java","-jar",str(svcPath)],stdout=subprocess.DEVNULL)
    
    while(True):
        try:
            reg.get("http://localhost/health")
            print("service started")
            break
        except Exception as e:
            print("waiting service to start...")
            time.sleep(1)
    
    return springProc
    

def profileMemService(svcProc,mem,toStop):
    
    while not toStop:
        mem_info = svcProc.memory_info()
        print(f"Memory RSS (Resident Set Size): {mem_info.rss / (1024 * 1024):.2f} MB")
        mem+=[mem_info.rss / (1024. * 1024.)]
        time.sleep(0.5)
    
def stressSvc():
    global toStop
    user = threading.Thread(target=httpUsers)
    user.start()

def httpUsers():
    global nrbLock, nrbreq
    while(not toStop):
        try:
            #print("sending req")
            res=reg.get("http://localhost")
            #print(f"response {res.status_code}")
            nrbLock.acquire()
            nrbreq+=1
            nrbLock.release()
        except:
            #traceback.print_exc()
            pass 

def stopService(svc):
    svc.terminate()
    svc.kill()
    outs, errs = svc.communicate()
    
def simulateKubeScaledown():
    global toStop
    toStop=False
    #startup service
    svc=startService()
    #start some users
    users=[]
    for it in range(3):
        users+=[threading.Thread(target=httpUsers)]
        users[-1].start()
    #shutdown the server
    time.sleep(2)
    subprocess.check_call(["pkill","-15","-f","k8testpod"])
    try:
        reg.get("http://localhost/health")
        print("liveness ok")
    except Exception as e:
        print("The serven has been shutdown")
    print("Waiting request to complete")
    users+=[threading.Thread(target=httpUsers)]
    users[-1].start()
    time.sleep(30)
    #checkfor error and througput
    print(f"#completed requests {nrbreq}")
    

def startUsers(nusers):
    users=[]
    for it in range(nusers):
        users+=[threading.Thread(target=httpUsers)]
        users[-1].start()
    return users

def polyfit(MEM=None,USERS=None):
    if(MEM is None or USERS is None):
        MEM=np.loadtxt("MEM.txt")
        USERS=np.loadtxt("USERS.txt")
        
    z = np.polyfit(USERS, MEM, 2)
    p = np.poly1d(z)
    return p
    

if __name__ == '__main__':
    
    # MEM=[]
    # USERS=[]
    # svc=startService()
    # for nusers in range(1,300,10):
    #     USERS+=[nusers]
    #     toStop=False
    #     #svc=startService()
    #     psutilsvc=ps.Process(svc.pid)
    #     #start users
    #     users=startUsers(nusers)
    #
    #     #start monitor
    #     endMnt=False
    #     mem=[]
    #     mnt = threading.Thread(target=profileMemService, args=(psutilsvc,mem,endMnt,))
    #     mnt.start()
    #     time.sleep(5)
    #     endMnt=True
    #     MEM+=[np.mean(mem)]
    #
    #     toStop=True
    #     #stopService(svc)
    #     time.sleep(2)
    #
    #     np.savetxt("MEM2.txt",MEM)
    #     np.savetxt("USERS2.txt",USERS)
    #
    # stopService(svc)
    
    USERS=np.loadtxt("USERS.txt")
    MEM=np.loadtxt("MEM.txt")
    
    p=polyfit(MEM[0:15],USERS[0:15])
    
    plt.figure()
    plt.plot(USERS,MEM,label="Data")
    plt.plot(USERS,p(USERS),label="Pred")
    plt.legend()
    plt.show()
    #simulateKubeScaledown()