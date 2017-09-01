package models;

import com.avaje.ebean.event.BeanPersistController;
import com.avaje.ebean.event.BeanPersistRequest;

import java.util.Set;

/**
 * Created by ggaona on 29/8/17.
 */
public class BeanPersistAdapter implements BeanPersistController {
    @Override
    public int getExecutionOrder() {
        return 0;
    }

    @Override
    public boolean isRegisterFor(Class<?> cls) {
        return true;
    }

    @Override
    public boolean preInsert(BeanPersistRequest<?> request) {
        return true;
    }

    @Override
    public boolean preUpdate(BeanPersistRequest<?> request) {
        return true;
    }

    @Override
    public boolean preDelete(BeanPersistRequest<?> request) {
        return true;
    }

    @Override
    public void postInsert(BeanPersistRequest<?> request) {
        // Post insert operation
        System.out.println("=== POST INSERT CALL == " + request.getBean().getClass());

    }

    @Override
    public void postUpdate(BeanPersistRequest<?> request) {
        System.out.println("=== POST UPDATE CALL == " + request.getBean().getClass());


    }

    @Override
    public void postDelete(BeanPersistRequest<?> request) {

    }

    @Override
    public void postLoad(Object bean, Set<String> includedProperties) {

    }
}
