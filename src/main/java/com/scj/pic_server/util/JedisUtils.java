package com.scj.pic_server.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.exceptions.JedisException;
import java.io.*;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author sunchangjunn
 * 2018年8月27日上午11:45:42
 */
public class JedisUtils {
	
	private  Logger logger = LoggerFactory.getLogger(JedisUtils.class);
	
	private  JedisPool jedisPool = null;
	 
	//Redis服务器IP
    private  String ADDR ;
    
    //Redis的端口号
    private  int PORT = 6379;
    
    //访问密码
    private  String AUTH ;
    
    //可用连接实例的最大数目，默认值为8；
    //如果赋值为-1，则表示不限制；如果pool已经分配了maxActive个jedis实例，则此时pool的状态为exhausted(耗尽)。
    private  int MAX_ACTIVE = 1024;
    
    //控制一个pool最多有多少个状态为idle(空闲的)的jedis实例，默认值也是8。
    private  int MAX_IDLE = 200;
    
    //等待可用连接的最大时间，单位毫秒，默认值为-1，表示永不超时。如果超过等待时间，则直接抛出JedisConnectionException；
    private  int MAX_WAIT = 10000;
    
    private  int TIMEOUT = 10000;
    
    //在borrow一个jedis实例时，是否提前进行validate操作；如果为true，则得到的jedis实例均是可用的；
    private  boolean TEST_ON_BORROW = true;
    
    public  JedisUtils(String host,String password) {
    	this.ADDR=host;
    	this.AUTH=password;
    	initJedis();
    }
    
    
    
    public  void  initJedis() {
        try {
            JedisPoolConfig config = new JedisPoolConfig();
            config.setMaxIdle(MAX_IDLE);
            config.setMaxWaitMillis(MAX_WAIT);
            config.setTestOnBorrow(TEST_ON_BORROW);
           jedisPool = new JedisPool(config, ADDR, PORT, TIMEOUT, AUTH);
//         jedisPool = new JedisPool(config, ADDR, PORT, TIMEOUT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    
	
	/**
	 * 获取资源
	 * @return
	 * @throws JedisException
	 */
	public  Jedis getResource() throws JedisException {
		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();

		} catch (JedisException e) {
			logger.warn("getResource.", e);
			returnBrokenResource(jedis);
			throw e;
		}
		return jedis;
		
	}

	public  void returnBrokenResource(Jedis jedis) {
		if (jedis != null) {
			jedisPool.returnBrokenResource(jedis);
		}
	}
	

	public  void returnResource(Jedis jedis) {
		if (jedis != null) {
			jedisPool.returnResource(jedis);
		}
	}


	/*获取缓存*/
	public  String get(String key) {
		String result = null;
		Jedis jedis = null;
		try {
			jedis = getResource();
			result = jedis.get(key);
		    if ("null".equals(result)){
		        return  null;
            }
		} catch (Exception e) {
			logger.info(e.getMessage());
		} finally {
			returnResource(jedis);
		}
		return result;
	}


	/**
	 * 向链表尾部插入元素
	 * @param key
	 * @param value
	 * @return
	 */
	public  Long rpush(String key,String value) {
		Long result = null;
		Jedis jedis = null;
		try {
			jedis = getResource();
			result= jedis.rpush(key,value);


		} catch (Exception e) {
			logger.info(e.getMessage());
		} finally {
			returnResource(jedis);
		}
		return result;
	}


	/**
	 * 移除并返回链表头部的元素
	 * @param key
	 * @return
	 */
	public  String lpop (String key) {
		String result = null;
		Jedis jedis = null;
		try {
			jedis = getResource();
			result= jedis.lpop (key);
		} catch (Exception e) {
			logger.info(e.getMessage());
		} finally {
			returnResource(jedis);
		}
		return result;
	}

	/*设置缓存*/
	public  String set(String key,Object value) {
		String result = null;
		Jedis jedis = null;
		try {
			jedis = getResource();
            if (value instanceof  String){
                result = jedis.set(key,(String) value);
            }else{
                result = jedis.set(key,JSONObject.toJSONString(value));
            }
		} catch (Exception e) {
			logger.info(e.getMessage());
		} finally {
			returnResource(jedis);
		}
		return result;
	}

	/*删除链表中的元素*/
	public  Long lrem(String key,long count,String value) {
		Long result = null;
		Jedis jedis = null;
		try {
			jedis = getResource();
			result = jedis.lrem (key,count,value);
		} catch (Exception e) {
			logger.info(e.getMessage());
		} finally {
			returnResource(jedis);
		}
		return result;
	}
	/*删除key*/
	public  Long del(String key) {
		Long result = null;
		Jedis jedis = null;
		try {
			jedis = getResource();
			result = jedis.del(key);
		} catch (Exception e) {
			logger.info(e.getMessage());
		} finally {
			returnResource(jedis);
		}
		return result;
	}

	/*查询key的生命周期(毫秒)*/
	public  Long pttl  (String key) {
		Long result = null;
		Jedis jedis = null;
		try {
			jedis = getResource();
			result = jedis.pttl  (key);
		} catch (Exception e) {
			logger.info(e.getMessage());
		} finally {
			returnResource(jedis);
		}
		return result;
	}


    /*设置缓存并设置过期时间(秒)*/
    public  String setex (String key,int seconds ,String value) {
        String result = null;
        Jedis jedis = null;
        try {
            jedis = getResource();
            result = jedis.setex (key,seconds,value);
        } catch (Exception e) {
            logger.info(e.getMessage());
        } finally {
            returnResource(jedis);
        }
        return result;
    }

    /*设置缓存并设置过期时间(耗秒)*/
    public  String psetex (String key,int seconds ,String value) {
        String result = null;
        Jedis jedis = null;
        try {
            jedis = getResource();
            result = jedis.psetex (key,seconds,value);
        } catch (Exception e) {
            logger.info(e.getMessage());
        } finally {
            returnResource(jedis);
        }
        return result;
    }

    /*设置某时间点过期(秒)*/
    public  Long expireAt (String key,long timestamp ,String value) {
        Long result = null;
        Jedis jedis = null;
        try {
            jedis = getResource();
            jedis.set(key,value);
            result = jedis.expireAt(key,timestamp);
        } catch (Exception e) {
            logger.info(e.getMessage());
        } finally {
            returnResource(jedis);
        }
        return result;
    }

    /*设置某时间点过期(毫秒)*/
    public  Long pexpireAt (String key,long timestamp ,String value) {
        Long result = null;
        Jedis jedis = null;
        try {
            jedis = getResource();
            jedis.set(key,value);
            result = jedis.pexpireAt(key,timestamp);
        } catch (Exception e) {
            logger.info(e.getMessage());
        } finally {
            returnResource(jedis);
        }
        return result;
    }
    /*移除key的过期时间*/
    public  Long persist (String key) {
        Long result = null;
        Jedis jedis = null;
        try {
            jedis = getResource();
            jedis.persist(key);

        } catch (Exception e) {
            logger.info(e.getMessage());
        } finally {
            returnResource(jedis);
        }
        return result;
    }
    /*设置对象列表缓存*/
    public  <T> boolean setList(String key, List<T> list) {
        Jedis jedis = null;
        String result = null;
        try {
            jedis = getResource();
            if (jedis != null) {
                for (T vz : list) {
                    if (vz instanceof String) {
                        jedis.lpush(key, (String) vz);
                    } else {
                        String  objectJson=JSONObject.toJSONString(vz);
                        jedis.lpush(key, objectJson);
                    }
                }
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            logger.info(e.getMessage());
            return false;
        } finally {
            returnResource(jedis);
        }
    }

    /*返回列表对象*/
	public  <T> List<T> getListEntity(String key, Class<T> entityClass) {
        Jedis jedis = null;
        String result = null;
		try {
            jedis = getResource();
			if (jedis != null) {
				List<String> valueJson = jedis.lrange(key, 0, -1);
				JSONArray json = new JSONArray();
				json.addAll(valueJson);
                return	JSONObject.parseArray(JSONObject.toJSONString(json),entityClass);
			} else {
				return null;
			}
		} catch (Exception e) {
            logger.info(e.getMessage());
			return null;
		} finally {
			returnResource(jedis);
		}
	}

    /*设置map*/
    public  <K, V> boolean setMap(String key, Map<String, V> map) {
        Jedis jedis = null;
        String result = null;
        try {
            jedis = getResource();
            if (jedis != null) {
                Set<Map.Entry<String, V>> entry = map.entrySet();
                for (Iterator<Map.Entry<String, V>> ite = entry.iterator(); ite.hasNext();) {
                    Map.Entry<String, V> kv = ite.next();
                    if (kv.getValue() instanceof String) {
                        jedis.hset(key, kv.getKey(), (String) kv.getValue());
                    } else if (kv.getValue() instanceof List) {
                        jedis.hset(key, kv.getKey(), JSONObject.toJSONString(kv.getValue()));
                    } else {
                        jedis.hset(key, kv.getKey(), JSONObject.toJSONString(kv.getValue()));
                    }
                }
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            returnResource(jedis);
        }
    }


    /*返回Map*/
	public  <K, V> Map<String, V> getMap(String key) {
            Jedis jedis = null;
		try {
            jedis = getResource();
			if (jedis != null) {
				Map<String, V> map = (Map<String, V>) jedis.hgetAll(key);
				return map;
			} else {
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			returnResource(jedis);
		}
	}
    /* incr(key)：名称为key的string增1操作 */
    public  boolean incr(String key) {
        Jedis jedis = null;
        try {
            jedis = getResource();
            jedis.incr(key);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            returnResource(jedis);
        }
    }

    

	public  Long publish (String channer,String message) {
		Long result = null;
		Jedis jedis = null;
		try {
			jedis = getResource();
			result= jedis.publish(channer,message);
			
			
		} catch (Exception e) {
			logger.info(e.getMessage());
		} finally {
			returnResource(jedis);
		}
		return result;
	}

	public  Long subscribe (JedisPubSub jedisPubSub,String channels) {
		Long result = null;
		Jedis jedis = null;
		try {
			jedis = getResource();
			 jedis.subscribe(jedisPubSub, channels);
		} catch (Exception e) {
			logger.info(e.getMessage());
		} finally {
			returnResource(jedis);
		}
		return result;
	}
    /**
     * incrby(key, integer)：名称为key的string增加integer
     */
    public  boolean incrBy(String key, int value) {
        Jedis jedis = null;
        try {
            jedis = getResource();
            jedis.incrBy(key, value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            returnResource(jedis);
        }
    }

    /**   * decr(key)：名称为key的string减1操作  */
    public  boolean decr(String key) {
        Jedis jedis = null;
        try {
            jedis = getResource();
            jedis.decr(key);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            returnResource(jedis);
        }
    }

    /**
     * decrby(key, integer)：名称为key的string减少integer
     */
    public  boolean decrBy(String key, int value) {
        Jedis jedis = null;
        try {
            jedis = getResource();
            jedis.decrBy(key, value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            returnResource(jedis);
        }
    }

    /*检查key是否存在*/
    public  boolean exists(String key) {
        boolean result = false;
        Jedis jedis = null;
        try {
            jedis = getResource();
            result =   jedis.exists(key);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            returnResource(jedis);
        }
    }

    /* 获取缓存 */
    public  String set(String key, File file) {
        String result = null;
        Jedis jedis = null;
        try {
            byte[] buffer = fileToBytes(file);
            jedis = getResource();
            result = jedis.set(key.getBytes(), buffer);
            if ("null".equals(result)) {
                return null;
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        } finally {
            returnResource(jedis);
        }
        return result;
    }

    /**
     * 获得指定文件的byte数组
     */
    private  byte[] fileToBytes(File file) {
        byte[] buffer = null;
        try {
            FileInputStream fis = new FileInputStream(file);
            ByteArrayOutputStream bos = new ByteArrayOutputStream(1000);
            byte[] b = new byte[1000];
            int n;
            while ((n = fis.read(b)) != -1) {
                bos.write(b, 0, n);
            }
            fis.close();
            bos.close();
            buffer = bos.toByteArray();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return buffer;
    }



    /*
     * 将redis中的数组读取出来转成输入流
     */
    public  InputStream getInputStream(String key) {
        if (key == null || key.equals("")) {
            return null;
        }
        Jedis jedis = null;
        try {
            jedis = getResource();
            byte[] json = jedis.get(key.getBytes());// 取出数组
            if (json != null && json.length > 0) {
                InputStream inputStream = new ByteArrayInputStream(json);// 转流
                return inputStream;
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        } finally {
            returnResource(jedis);
        }
        return null;

    }

    /**
     * 获取byte数组
     * @param key
     * @return
     */
    public  byte[]  getBytes(String key){
        byte[] result = null;
        Jedis jedis = null;
        try {
            jedis = getResource();
            result =   jedis.get(key.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            returnResource(jedis);
        }
        return  result;
    }

    /**
     * 获取流
     * @param key
     * @return
     */
    public  InputStream  getStream(String key){
        InputStream result = null;
        Jedis jedis = null;
        try {
            jedis = getResource();
          byte[]  bytes =   jedis.get(key.getBytes());
          if(null != bytes){
              result = new ByteArrayInputStream(bytes);
          }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            returnResource(jedis);
        }
        return  result;
    }

    /*向无序集合中添加元素*/
    public  boolean sadd(String key,String value) {
      boolean result =false;
        Jedis jedis = null;
        try {
            jedis = getResource();
            jedis.sadd(key,value);
            result=true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            returnResource(jedis);
        }
        return result;
    }
    /*删除无序集合中某个元素*/
    public  boolean srem(String key,String value) {
        boolean result =false;
        Jedis jedis = null;
        try {
            jedis = getResource();
            jedis.srem(key,value);
            result=true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            returnResource(jedis);
        }
        return result;
    }
    /*判断无序集合中是否有某元素存在*/
    public  boolean sismember(String key,String value) {
        boolean result =false;
        Jedis jedis = null;
        try {
            jedis = getResource();
            result =  jedis.sismember(key,value);

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            returnResource(jedis);
        }
        return result;
    }

    /**
     * 向有序集合中添加元素
     * @param key
     * @param soure  例如:收听数
     * @param value
     * @return
     */
    public  Long zadd(String key,double soure,String value) {
        Long result =null;/*返回值:
                                    被成功添加的新成员的数量，不包括那些被更新的、已经存在的成员。*/
        Jedis jedis = null;
        try {
            jedis = getResource();
            result =   jedis.zadd(key,soure,value);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            returnResource(jedis);
        }
        return result;
    }

    /*删除有序集合中某个元素*/
    public  Long zadd(String key,String value) {
        Long result =null;/*返回值:
                                    被成功添加的新成员的数量，不包括那些被更新的、已经存在的成员。*/
        Jedis jedis = null;
        try {
            jedis = getResource();
            result =  jedis.zrem(key,value);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            returnResource(jedis);
        }
        return result;
    }


    /*查询值在有序集合中的排名*/
    public  Long zrevrank (String key,String value) {
        Long result =null;/*返回值:
                                    被成功添加的新成员的数量，不包括那些被更新的、已经存在的成员。*/
        Jedis jedis = null;
        try {
            jedis = getResource();
            result =  jedis.zrevrank (key,value);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            returnResource(jedis);
        }
        return result;
    }


    /*查询值在区间的数量:如薪水在多少到多少有多少数量*/
    public  Long zcount  (String key,double min,double max ) {
        Long result =null;//score 值在 min 和 max 之间的成员的数量。
        Jedis jedis = null;
        try {
            jedis = getResource();
            result =  jedis.zcount (key,min,max);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            returnResource(jedis);
        }
        return result;
    }

    /*返回名次在开始和结束之间的元素*/
    public  Set<String> zrange  (String key,long start,long end ) {
        Set<String> result =null;//score 值在 min 和 max 之间的成员的数量。
        Jedis jedis = null;
        try {
            jedis = getResource();
            result =  jedis.zrange (key,start,end);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            returnResource(jedis);
        }
        return result;
    }

}
