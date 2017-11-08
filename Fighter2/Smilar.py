import urlparse 
import hashlib 
hash_size  = 199999 

def parse(url): 
	tmp = urlparse.urlparse(url)
	scheme = tmp[0];netloc = tmp[1]; path = tmp[2][1:];query = tmp[4]

	if len(path.split('/')[-1].split('.')) > 1:
		tail = path.split('/')[-1].split('.')[-1]
	elif len(path.split('/')) == 1:
		tail = path
	else:
		tail = '1'
		
	tail = tail.lower()
	path_length = len(path.split('/')) - 1
	path_value = 0
	path_list = path.split('/')[:-1] + [tail]
	
	for i in range(path_length - 1):
		if path_length - i == 0:
			path_value += hash(path_list[path_length - i])%98765
		else:
			path_value += len(path_list[path_length - i])*(10**(i + 1))
	netloc_value = hash(hashlib.new("md5",netloc).hexdigest())%hash_size
	url_value = hash(hashlib.new("md5",str(path_value - netloc_value)).hexdigest())%hash_size
	return url_value
	
url = 'http://auto.sohu.com/s2007/5730/s249067983/index4.html'
print(parse(url))

url = 'http://auto.sohu.com/s2007/5730/s249067983/index4.html'
print(parse(url))