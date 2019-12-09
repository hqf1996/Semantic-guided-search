# -*- coding: utf-8 -*-
import config
import models
import tensorflow as tf
import numpy as np
import json
import os
import codecs

iteration_num = 500
dimension = 200

con = config.Config()
con.set_in_path("./benchmarks/experiment/automobile/part_0.1_0.4_0.8/")


# con.set_test_link_prediction(True)
# con.set_test_triple_classification(True)
con.set_work_threads(4)
con.set_dimension(dimension)
con.set_import_files("./my/experimentModel/transE/automobile/part_0.1_0.4_0.8/iteration%d/%d/model.vec.tf" %(iteration_num, dimension))

con.init()
con.set_model(models.TransE)
# con.test()

# 打印关系之间的相似度
relation_file = codecs.open(os.path.join(con.in_path, "relation2id.txt"), "r", encoding='utf-8')
count = int(relation_file.readline().strip())
d = list()
for line in relation_file:
	d.append(line.split('\t')[0])

fp_w = codecs.open("./result/experiment/automobile/iteration%d/part_0.1_0.4_0.8_%d.txt" %(iteration_num, dimension), "wb", encoding='utf-8')


for i in range (count):
	print "====================It is %s======================" % d[i]
	if True:
		fp_w.write("====================It is %s======================\n" % d[i])
		result = con.predict_relation(i, count=count)
		sortIndex = np.argsort(result)
		print result
		for j in range(count):
			print "第%d位 : %s-------------%lf" %(j+1, d[sortIndex[j]], 1-result[sortIndex[j]])
			fp_w.write("第%d位 : %s-------------%lf\n" %(j+1, d[sortIndex[j]], 1-result[sortIndex[j]]))
fp_w.close()
