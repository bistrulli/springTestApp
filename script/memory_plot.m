clear
users=table2array(readtable("USERS.txt"));
mem=table2array(readtable("MEM.txt"));

p=polyfit(users(1:14),mem(1:14),2);

f=figure('units','normalized','outerposition',[0 0 1 1]);
hold on
plot(users,mem,"LineStyle","--","LineWidth",2)
plot(users(1:14),polyval(p,users(1:14)),"LineStyle","-.","LineWidth",2)
plot(users(14:end),polyval(p,users(14:end)),'r*',"LineWidth",2)
hold on
box on
grid on
xlabel("Concurrency Level")
ylabel("Memory(MB)")
legend("Dataset","Mem_f prediction on learning points","Mem_f predictions on unseen points","Location","southeast")
fontsize(gcf,36,"pixels")
exportgraphics(f,"./memory_plot.pdf");
close(f);