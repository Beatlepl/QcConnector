net use \\scm-trees.eng.vmware.com\trees /user:bpaul
net use  \\build-apps.eng.vmware.com\apps /user:bpaul
set PATH=\\build-apps.eng.vmware.com\apps\bin;F:\toolchain\win32\bin;%PATH%
set PATH=Q:\bin;F:\toolchain\win32\bin;%PATH%
set P4CLIENT=bpaul_BP-DESKTOP
set P4PORT=perforce.eng.vmware.com:1666
set TCROOT=F:\toolchain
set /P changelist= enter change list no :
p5 diff --strip-trailing-cr -du -c %changelist% >  %changelist%.html