package com.precisionhawk.ams.dao;

import com.precisionhawk.ams.dao.fs.YamlFileSecurityDao;
import com.precisionhawk.ams.dao.rdbms.SecurityDaoImpl;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import org.apache.ibatis.session.SqlSession;

/**
 *
 * @author pchapman
 */
@Named
public class SecurityDaoFactory implements Provider<SecurityDao> {

    private final Object LOCK = new Object();
    
    @Inject private SecurityDaoConfig config;
    @Inject private SqlSession sess;
    
    private SecurityDao dao;
    
    @Override
    public SecurityDao get() {
        synchronized (LOCK) {
            if (dao == null) {
                if (YamlFileSecurityDao.class.getName().equals(config.getSecurityDaoImplementation())) {
                    if (config.getSecurityDataFile() == null || config.getSecurityDataFile().isEmpty()) {
                        throw new IllegalArgumentException("No config file found for YamlFileSecurityDao.");
                    }
                    YamlFileSecurityDao impl = new YamlFileSecurityDao();
                    impl.configure(config.getSecurityDataFile());
                    dao = impl;
                } else if (SecurityDaoImpl.class.getName().equals(config.getSecurityDaoImplementation())) {
                    if (sess == null) {
                        throw new IllegalArgumentException("No SQL Session configured.  It is required by the security Dao Implementation.");
                    }
                    SecurityDaoImpl impl = new SecurityDaoImpl();
                    impl.setSqlSession(sess);
                    dao = impl;
                }
            }
        }
        return dao;
    }
}
