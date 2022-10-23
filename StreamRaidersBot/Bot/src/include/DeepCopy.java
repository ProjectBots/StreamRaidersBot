package include;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class DeepCopy {

	/**
	 * returns a copy of obj<br>
	 * it will only be a true deepcopy if all mutable fields implement {@link Cloneable} and return a deepcopy when clone() is called<br>
	 * <br>
	 * all elements of arrays will be cloned
	 * @param <T>
	 * @param obj
	 * @return the result
	 */
	public static <T> T copyObject(T obj) {
		if(obj instanceof Cloneable) {
			final Object result;
			if(obj.getClass().isArray()) {
				final Class<?> componentType = obj.getClass().getComponentType();
				int length = Array.getLength(obj);
				result = Array.newInstance(componentType, length);
				if(componentType.isPrimitive()) {
					while(length-- > 0)
						Array.set(result, length, Array.get(obj, length));
				} else {
					while(length-- > 0)
						Array.set(result, length, copyObject(Array.get(obj, length)));
				}
			} else {
				try {
					Method clone = obj.getClass().getMethod("clone");
					clone.setAccessible(true);
					result = clone.invoke(obj);
				} catch (NoSuchMethodException | SecurityException | IllegalAccessException | InvocationTargetException e) {
					throw new RuntimeException("failed to invoke clone", e);
				}
			}
			@SuppressWarnings("unchecked") // OK because input is of type T
			final T checked = (T) result;
			return checked;
		}
		return obj;
	}
	
	/**
	 * deepcopies all non static fields from donor to the recipient
	 * @param <T>
	 * @param rec recipient
	 * @param don donor
	 * @return the recipient
	 */
	public static <T> T copyAllFields(T rec, T don) {
		Field[] fields = rec.getClass().getDeclaredFields();
		for(int i=0; i<fields.length; i++) {
			if(Modifier.isStatic(fields[i].getModifiers()))
				continue;
			
			//	allows to override final fields
			fields[i].setAccessible(true);
			
			try {
				fields[i].set(rec, DeepCopy.copyObject(fields[i].get(don)));
			} catch (IllegalAccessException | RuntimeException e) {
				throw new RuntimeException("failed to clone "+fields[i].getName(), e);
			}
		}
		return rec;
	}

}
