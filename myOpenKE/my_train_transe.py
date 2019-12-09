# -*- coding: utf-8 -*-
import config
import models
import tensorflow as tf
import numpy as np
import time

start = time.time()
con = config.Config()
con.set_in_path("./benchmarks/experiment/automobile/part_0.1_0.4_0.8/")



iteration_num = 500
dimension = 200

con.set_work_threads(8)
con.set_train_times(iteration_num)
con.set_nbatches(25)
# con.set_nbatches(1000)
con.set_alpha(0.001)
con.set_margin(1.0)
con.set_bern(0)
con.set_dimension(dimension)
con.set_ent_neg_rate(1)
con.set_rel_neg_rate(0)
con.set_opt_method("SGD")

#Models will be exported via tf.Saver() automatically.
con.set_export_files("./my/experimentModel/automobile/part_0.1_0.4_0.8/iteration%d/%d/model.vec.tf" %(iteration_num, dimension), 0)


#Model parameters will be exported to json files automatically.
con.set_out_files("./my/experimentModel/automobile/part_0.1_0.4_0.8/iteration%d/%d/embedding.vec.json" %(iteration_num, dimension))


#Initialize experimental settings.
con.init()
#Set the knowledge embedding model
# con.set_model(models.TransE)
con.set_model(models.TransE)
#Train the model.
try:
    con.run()
finally:
    end = time.time()
    print "train time is: %lf\n" %(end-start)
