#coding:utf-8
import numpy as np
import tensorflow as tf
import os
import time
import datetime
import ctypes
import json
import codecs

class Config(object):
	def __init__(self):
		self.lib = ctypes.cdll.LoadLibrary("./release/Base.so")
		self.lib.sampling.argtypes = [ctypes.c_void_p, ctypes.c_void_p, ctypes.c_void_p, ctypes.c_void_p, ctypes.c_int64, ctypes.c_int64, ctypes.c_int64]
		self.lib.getHeadBatch.argtypes = [ctypes.c_void_p, ctypes.c_void_p, ctypes.c_void_p]
		self.lib.getOneBatch.argtypes = [ctypes.c_void_p, ctypes.c_void_p, ctypes.c_void_p, ctypes.c_int64, ctypes.c_int64, ctypes.c_int64, ctypes.c_int64]
		self.lib.getTailBatch.argtypes = [ctypes.c_void_p, ctypes.c_void_p, ctypes.c_void_p]
		self.lib.getSimilar.argtypes = [ctypes.c_void_p, ctypes.c_int64, ctypes.c_int64, ctypes.c_int64, ctypes.c_void_p, ctypes.c_int64]
		self.lib.getSimilar.restype = ctypes.c_int64
		self.lib.testHead.argtypes = [ctypes.c_void_p]
		self.lib.testTail.argtypes = [ctypes.c_void_p]
		self.lib.getTestBatch.argtypes = [ctypes.c_void_p, ctypes.c_void_p, ctypes.c_void_p, ctypes.c_void_p, ctypes.c_void_p, ctypes.c_void_p]
		self.lib.getValidBatch.argtypes = [ctypes.c_void_p, ctypes.c_void_p, ctypes.c_void_p, ctypes.c_void_p, ctypes.c_void_p, ctypes.c_void_p]
		self.lib.getBestThreshold.argtypes = [ctypes.c_void_p, ctypes.c_void_p]
		self.lib.test_triple_classification.argtypes = [ctypes.c_void_p, ctypes.c_void_p]
		self.test_flag = False
		self.in_path = None   # 读入的数据路径
		self.out_path = None
		self.bern = 0
		self.hidden_size = 100
		self.ent_size = self.hidden_size
		self.rel_size = self.hidden_size
		self.train_times = 0  #迭代次数
		self.margin = 1.0
		self.nbatches = 100  # 采样的批次
		self.negative_ent = 1
		self.negative_rel = 0
		self.workThreads = 1
		self.alpha = 0.001
		self.lmbda = 0.000
		self.log_on = 1    # 打印输出的flag
		self.exportName = None  # 保存模型的路径
		self.importName = None	# 载入模型的路径
		self.export_steps = 0
		self.opt_method = "SGD"
		self.optimizer = None
		self.test_link_prediction = False
		self.test_triple_classification = False
		self.predict_node = False
		self.predict_edge = False
		self.predict_simliar = False
	def init(self):
		self.trainModel = None
		if self.in_path != None:
			self.lib.setInPath(ctypes.create_string_buffer(self.in_path, len(self.in_path) * 2))
			self.lib.setBern(self.bern)
			self.lib.setWorkThreads(self.workThreads)
			self.lib.randReset()
			self.lib.importTrainFiles()
			self.relTotal = self.lib.getRelationTotal() # 得到边的总数
			self.entTotal = self.lib.getEntityTotal()   # 得到节点的总数
			self.trainTotal = self.lib.getTrainTotal()  # 得到训练集的总数
			self.testTotal = self.lib.getTestTotal()    # 得到测试集的总数
			self.validTotal = self.lib.getValidTotal()  # 得到验证集的总数
			self.batch_size = self.lib.getTrainTotal() / self.nbatches  # 得到一次取到的数量
			self.batch_seq_size = self.batch_size * (1 + self.negative_ent + self.negative_rel)
			self.batch_h = np.zeros(self.batch_size * (1 + self.negative_ent + self.negative_rel), dtype = np.int64)
			self.batch_t = np.zeros(self.batch_size * (1 + self.negative_ent + self.negative_rel), dtype = np.int64)
			self.batch_r = np.zeros(self.batch_size * (1 + self.negative_ent + self.negative_rel), dtype = np.int64)
			self.batch_y = np.zeros(self.batch_size * (1 + self.negative_ent + self.negative_rel), dtype = np.float32)
			self.batch_h_addr = self.batch_h.__array_interface__['data'][0]
			self.batch_t_addr = self.batch_t.__array_interface__['data'][0]
			self.batch_r_addr = self.batch_r.__array_interface__['data'][0]
			self.batch_y_addr = self.batch_y.__array_interface__['data'][0]
		if self.test_link_prediction:
			self.lib.importTestFiles()
			self.test_h = np.zeros(self.lib.getEntityTotal(), dtype = np.int64)
			self.test_t = np.zeros(self.lib.getEntityTotal(), dtype = np.int64)
			self.test_r = np.zeros(self.lib.getEntityTotal(), dtype = np.int64)
			self.test_h_addr = self.test_h.__array_interface__['data'][0]
			self.test_t_addr = self.test_t.__array_interface__['data'][0]
			self.test_r_addr = self.test_r.__array_interface__['data'][0]
		if self.test_triple_classification:
			self.lib.importTestFiles()
			self.lib.importTypeFiles()

			self.test_pos_h = np.zeros(self.lib.getTestTotal(), dtype = np.int64)
			self.test_pos_t = np.zeros(self.lib.getTestTotal(), dtype = np.int64)
			self.test_pos_r = np.zeros(self.lib.getTestTotal(), dtype = np.int64)
			self.test_neg_h = np.zeros(self.lib.getTestTotal(), dtype = np.int64)
			self.test_neg_t = np.zeros(self.lib.getTestTotal(), dtype = np.int64)
			self.test_neg_r = np.zeros(self.lib.getTestTotal(), dtype = np.int64)
			self.test_pos_h_addr = self.test_pos_h.__array_interface__['data'][0]
			self.test_pos_t_addr = self.test_pos_t.__array_interface__['data'][0]
			self.test_pos_r_addr = self.test_pos_r.__array_interface__['data'][0]
			self.test_neg_h_addr = self.test_neg_h.__array_interface__['data'][0]
			self.test_neg_t_addr = self.test_neg_t.__array_interface__['data'][0]
			self.test_neg_r_addr = self.test_neg_r.__array_interface__['data'][0]

			self.valid_pos_h = np.zeros(self.lib.getValidTotal(), dtype = np.int64)
			self.valid_pos_t = np.zeros(self.lib.getValidTotal(), dtype = np.int64)
			self.valid_pos_r = np.zeros(self.lib.getValidTotal(), dtype = np.int64)
			self.valid_neg_h = np.zeros(self.lib.getValidTotal(), dtype = np.int64)
			self.valid_neg_t = np.zeros(self.lib.getValidTotal(), dtype = np.int64)
			self.valid_neg_r = np.zeros(self.lib.getValidTotal(), dtype = np.int64)
			self.valid_pos_h_addr = self.valid_pos_h.__array_interface__['data'][0]
			self.valid_pos_t_addr = self.valid_pos_t.__array_interface__['data'][0]
			self.valid_pos_r_addr = self.valid_pos_r.__array_interface__['data'][0]
			self.valid_neg_h_addr = self.valid_neg_h.__array_interface__['data'][0]
			self.valid_neg_t_addr = self.valid_neg_t.__array_interface__['data'][0]
			self.valid_neg_r_addr = self.valid_neg_r.__array_interface__['data'][0]

		if self.predict_simliar:
			self.lib.importTrainFiles()


	def get_ent_total(self):
		return self.entTotal

	def get_rel_total(self):
		return self.relTotal

	def set_lmbda(self, lmbda):
		self.lmbda = lmbda

	def set_optimizer(self, optimizer):
		self.optimizer = optimizer

	def set_opt_method(self, method):
		self.opt_method = method

	def set_test_link_prediction(self, flag):
		self.test_link_prediction = flag

	def set_test_triple_classification(self, flag):
		self.test_triple_classification = flag

	def set_predict_simliar(self, flag):
		"""
		三元组的相近预测的开启
		:return: 
		"""
		self.predict_simliar = flag

	def set_log_on(self, flag):
		self.log_on = flag

	def set_alpha(self, alpha):
		self.alpha = alpha

	def set_in_path(self, path):
		self.in_path = path

	def set_out_files(self, path):
		self.out_path = path

	def set_bern(self, bern):
		self.bern = bern

	def set_dimension(self, dim):
		self.hidden_size = dim
		self.ent_size = dim
		self.rel_size = dim

	def set_ent_dimension(self, dim):
		self.ent_size = dim

	def set_rel_dimension(self, dim):
		self.rel_size = dim

	def set_train_times(self, times):
		self.train_times = times

	def set_nbatches(self, nbatches):
		self.nbatches = nbatches

	def set_margin(self, margin):
		self.margin = margin

	def set_work_threads(self, threads):
		self.workThreads = threads

	def set_ent_neg_rate(self, rate):
		self.negative_ent = rate

	def set_rel_neg_rate(self, rate):
		self.negative_rel = rate

	def set_import_files(self, path):
		self.importName = path

	def set_export_files(self, path, steps = 0):
		'''
		设置输出的路径和多少次迭代之后保存一次模型
		:param path:设置保存模型的路径
		:param steps: 设置多少次迭代后保存模型，0表示只保存最后一次输出的模型，默认为0
		:return: 
		'''
		self.exportName = path
		self.export_steps = steps

	def set_export_steps(self, steps):
		self.export_steps = steps

	def sampling(self):
		self.lib.sampling(self.batch_h_addr, self.batch_t_addr, self.batch_r_addr, self.batch_y_addr, self.batch_size, self.negative_ent, self.negative_rel)

	def save_tensorflow(self):
		with self.graph.as_default():
			with self.sess.as_default():
				self.saver.save(self.sess, self.exportName)

	def restore_tensorflow(self):
		with self.graph.as_default():
			with self.sess.as_default():
				self.saver.restore(self.sess, self.importName)


	def export_variables(self, path = None):
		with self.graph.as_default():
			with self.sess.as_default():
				if path == None:
					self.saver.save(self.sess, self.exportName)
				else:
					self.saver.save(self.sess, path)

	def import_variables(self, path = None):
		with self.graph.as_default():
			with self.sess.as_default():
				if path == None:
					self.saver.restore(self.sess, self.importName)
				else:
					self.saver.restore(self.sess, path)

	def get_parameter_lists(self):
		return self.trainModel.parameter_lists

	def get_parameters_by_name(self, var_name):
		with self.graph.as_default():
			with self.sess.as_default():
				if var_name in self.trainModel.parameter_lists:
					return self.sess.run(self.trainModel.parameter_lists[var_name])
				else:
					return None

	def get_parameters(self, mode = "numpy"):
		res = {}
		lists = self.get_parameter_lists()
		for var_name in lists:
			if mode == "numpy":
				res[var_name] = self.get_parameters_by_name(var_name)
			else:
				res[var_name] = self.get_parameters_by_name(var_name).tolist()
		return res

	def save_parameters(self, path = None):
		if path == None:
			path = self.out_path
		f = open(path, "w")
		# f.write(json.dumps(self.get_parameters("list")))
		json.dump(self.get_parameters("list"), f)
		f.close()

	def set_parameters_by_name(self, var_name, tensor):
		with self.graph.as_default():
			with self.sess.as_default():
				if var_name in self.trainModel.parameter_lists:
					self.trainModel.parameter_lists[var_name].assign(tensor).eval()

	def set_parameters(self, lists):
		for i in lists:
			self.set_parameters_by_name(i, lists[i])

	def set_model(self, model):
		self.model = model
		self.graph = tf.Graph()
		with self.graph.as_default():
			self.sess = tf.Session()
			with self.sess.as_default():
				initializer = tf.contrib.layers.xavier_initializer(uniform = True)
				with tf.variable_scope("model", reuse=None, initializer = initializer):
					self.trainModel = self.model(config = self)
					if self.optimizer != None:
						pass
					elif self.opt_method == "Adagrad" or self.opt_method == "adagrad":
						self.optimizer = tf.train.AdagradOptimizer(learning_rate = self.alpha, initial_accumulator_value=1e-20)
					elif self.opt_method == "Adadelta" or self.opt_method == "adadelta":
						self.optimizer = tf.train.AdadeltaOptimizer(self.alpha)
					elif self.opt_method == "Adam" or self.opt_method == "adam":
						self.optimizer = tf.train.AdamOptimizer(self.alpha)
					else:
						self.optimizer = tf.train.GradientDescentOptimizer(self.alpha)
					grads_and_vars = self.optimizer.compute_gradients(self.trainModel.loss)
					self.train_op = self.optimizer.apply_gradients(grads_and_vars)
				self.saver = tf.train.Saver()
				self.sess.run(tf.initialize_all_variables())

	def train_step(self, batch_h, batch_t, batch_r, batch_y):
		feed_dict = {
			self.trainModel.batch_h: batch_h,
			self.trainModel.batch_t: batch_t,
			self.trainModel.batch_r: batch_r,
			self.trainModel.batch_y: batch_y
		}
		_, loss = self.sess.run([self.train_op, self.trainModel.loss], feed_dict)
		return loss

	def test_step(self, test_h, test_t, test_r):
		feed_dict = {
			self.trainModel.predict_h: test_h,
			self.trainModel.predict_t: test_t,
			self.trainModel.predict_r: test_r,
		}
		predict = self.sess.run(self.trainModel.predict, feed_dict)
		return predict

	def run(self):
		with self.graph.as_default():
			with self.sess.as_default():
				if self.importName != None:
					self.restore_tensorflow()
				for times in range(self.train_times):
					res = 0.0
					for batch in range(self.nbatches):
						self.sampling()
						res += self.train_step(self.batch_h, self.batch_t, self.batch_r, self.batch_y)
					if self.log_on:  # 是否输出日志，默认为1，输出
						print times
						print res
					if self.exportName != None and (self.export_steps!=0 and times % self.export_steps == 0):
						self.save_tensorflow()
				if self.exportName != None:
					self.save_tensorflow()
				if self.out_path != None:
					self.save_parameters(self.out_path)

	def test(self):
		with self.graph.as_default():
			with self.sess.as_default():
				if self.importName != None:  # 读入模型
					self.restore_tensorflow()
				if self.test_link_prediction:
					total = self.lib.getTestTotal()
					for times in range(total):
						# 计算每一条的排名，以及该条是否排在前10（假定测试集的数据是真的，那么体现模型好坏是按照该条记录在是否在模型计算结果按照从小到大排列是否排名靠前，是否在前10）
						self.lib.getHeadBatch(self.test_h_addr, self.test_t_addr, self.test_r_addr)
						res = self.test_step(self.test_h, self.test_t, self.test_r)
						self.lib.testHead(res.__array_interface__['data'][0])

						self.lib.getTailBatch(self.test_h_addr, self.test_t_addr, self.test_r_addr)
						res = self.test_step(self.test_h, self.test_t, self.test_r)
						self.lib.testTail(res.__array_interface__['data'][0])
						if self.log_on:
							print times
					self.lib.test_link_prediction()
				if self.test_triple_classification:   # 测试分类
					# 从验证集中读取正确的三元组，并改变其中的尾结点，得到负三元组
					self.lib.getValidBatch(self.valid_pos_h_addr, self.valid_pos_t_addr, self.valid_pos_r_addr, self.valid_neg_h_addr, self.valid_neg_t_addr, self.valid_neg_r_addr)
					# 验证集中正的三元组的模型预测值
					res_pos = self.test_step(self.valid_pos_h, self.valid_pos_t, self.valid_pos_r)
					# 验证集中负的三元组的模型预测值
					res_neg = self.test_step(self.valid_neg_h, self.valid_neg_t, self.valid_neg_r)
					# 得到最好的区分阈值
					self.lib.getBestThreshold(res_pos.__array_interface__['data'][0], res_neg.__array_interface__['data'][0])

					# 从测试集中读取正确的三元组，并改变其中的尾结点，得到负三元组
					self.lib.getTestBatch(self.test_pos_h_addr, self.test_pos_t_addr, self.test_pos_r_addr, self.test_neg_h_addr, self.test_neg_t_addr, self.test_neg_r_addr)
					# 测试集中正的三元组的模型预测值
					res_pos = self.test_step(self.test_pos_h, self.test_pos_t, self.test_pos_r)
					# 测试集中负的三元组的模型预测值
					res_neg = self.test_step(self.test_neg_h, self.test_neg_t, self.test_neg_r)
					# 根据阈值得到分类的正确率
					self.lib.test_triple_classification(res_pos.__array_interface__['data'][0], res_neg.__array_interface__['data'][0])


	def init_predict_similar(self, which):
		if self.predict_node == False and (which == 0 or which == 1):
			self.predict_node_h = np.zeros(self.lib.getEntityTotal(), dtype=np.int64)
			self.predict_node_t = np.zeros(self.lib.getEntityTotal(), dtype=np.int64)
			self.predict_node_r = np.zeros(self.lib.getEntityTotal(), dtype=np.int64)
			self.predict_node_h_addr = self.predict_node_h.__array_interface__['data'][0]
			self.predict_node_t_addr = self.predict_node_t.__array_interface__['data'][0]
			self.predict_node_r_addr = self.predict_node_r.__array_interface__['data'][0]
			self.predict_node = True
		elif self.predict_edge == False and which == 2:
			self.predict_edge_h = np.zeros(self.lib.getRelationTotal(), dtype=np.int64)
			self.predict_edge_t = np.zeros(self.lib.getRelationTotal(), dtype=np.int64)
			self.predict_edge_r = np.zeros(self.lib.getRelationTotal(), dtype=np.int64)
			self.predict_edge_h_addr = self.predict_edge_h.__array_interface__['data'][0]
			self.predict_edge_t_addr = self.predict_edge_t.__array_interface__['data'][0]
			self.predict_edge_r_addr = self.predict_edge_r.__array_interface__['data'][0]
			self.predict_edge = True

	def __predict_one(self, h, t, r, which=0):
		"""
		得到需要预测的节点的各个模型值
		:param h: 
		:param t: 
		:param r: 
		:param which: 
		:return: 
		"""
		self.init_predict_similar(which)
		resultPair = list()
		with self.graph.as_default():
			with self.sess.as_default():
				if self.importName != None:
					self.restore_tensorflow()
				if which == 0 or which == 1:
					self.lib.getOneBatch(self.predict_node_h_addr, self.predict_node_t_addr, self.predict_node_r_addr,
										 ctypes.c_int64(h), ctypes.c_int64(t), ctypes.c_int64(r), ctypes.c_int64(which))
					res = self.test_step(self.predict_node_h, self.predict_node_t, self.predict_node_r)
				else:
					self.lib.getOneBatch(self.predict_edge_h_addr, self.predict_edge_t_addr, self.predict_edge_r_addr,
										 ctypes.c_int64(h), ctypes.c_int64(t), ctypes.c_int64(r), ctypes.c_int64(which))
					res = self.test_step(self.predict_edge_h, self.predict_edge_t, self.predict_edge_r)
		return res

	def predict_relation(self, relation = 0, count=10):
		"""
		预测关系
		:param relation: range(0, 9)
		:return: 
		"""
		fp_r = codecs.open(self.in_path + "train2id.txt", "r", encoding='utf-8')
		fp_r.readline()
		n = 0
		count = 0
		res = None
		for line in fp_r:
			print "count", count
			count = count + 1
			head, tail, edge = line.strip().split(" ")
			if int(edge) == relation:
				n = n + 1
				print "n--------> %d %d" % (n,int(edge))
				if n == 1:
					res = self.__predict_one(int(head), int(tail), relation, 2)
				else:
					res = res + self.__predict_one(int(head), int(tail), relation, 2)
                return res/(n*1.0)	
		# return self.sort_predict_edge(res/(n*1.0), relation, count)

	def get_similar(self, h, t, r, which=0):
		"""
		 一个三元组的相近预测
		:param h: 头结点
		:param t: 尾节点
		:param r: 边
		:param which: 标记，预测头节点，尾节点还是边,0表示头结点，1表示尾节点，2表示边
		:return: 
		"""
		p = h
		if which == 0:
			p = h
		elif which == 1:
			p = t
		elif which == 2:
			p = r
		else:
			raise AttributeError
		res = self.__predict_one(h, t, r, which)
		self.sort_predict_edge(res, p)

	def sort_predict_edge(self, result, p, count=10):
		sortIndex = np.argsort(result)
		return result, sortIndex
                """
		print "the input triple index is ",
		print np.where(sortIndex == p)
		file_path = self.in_path + "relation2id.txt"
		fp_r = codecs.open(file_path, "r", encoding='utf-8')
		fp_r.readline()
		d = list()
		for line in fp_r:
			d.append(line.strip())
		fp_r.close()
		# d = ["assembly", "designer", "establish", "location", "manufacturer", "place_of_origin", "production",
		# 	 "service", "supplier", "type", "fakeRelation"]
		count = min(count, len(d))
		for i in range(count):
			print "第%d位 : %s-------------%lf" % (i + 1, d[sortIndex[i]], result[sortIndex[i]])

		"""

		# 		count = self.lib.getSimilar(res.__array_interface__['data'][0], h, t, r, result.__array_interface__['data'][0], which)
		# 		result = result[:count]
		# 		if which == 0:
		# 			print res[h]
		# 		elif which == 1:
		# 			print res[t]
		# 		else:
		# 			print res[r]
		# 		#self.__show_simalar_result(result)
		# 		# resultPair = [(x, res[x]) for x in result]
		# 		resultPair = sorted([(x, res[x]) for x in result], key=lambda x: x[1])
		# self.__show_simalar_result(resultPair)
		# return resultPair

	def __show_simalar_result(self, resultPair):
		print "total find similar %d ids, they are : " % len(resultPair)
		print "-------------------------------------------------------------------"
		for each in resultPair:
			print "(%d %d) " % (each[0], each[1]),
			# print each,
			# print " ",
		print "\n-------------------------------------------------------------------"

