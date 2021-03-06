library(ROCR)
files <- list.files(pattern="*.txt",full.names=T, recursive=FALSE)
lapply(files,function(x){
	print(x)
	data <- readLines(x)
	predictionStrings <- strsplit (data[1],",")
	predictions <- unlist(lapply(predictionStrings,as.numeric))
	labelStrings <- strsplit(data[2], ",")
	labels <- unlist(lapply(labelStrings,as.numeric))
	pred <- prediction(predictions, labels,label.ordering = c(0,1))
	perf <- performance(pred, measure = "tpr", x.measure = "fpr") 
	plot(perf,colorize=TRUE,lwd=3, main=x,type="s")
	abline(a=0,b=1)
})