package com.example.businessbase.views

import java.lang.Exception

object ReflectHelper {
    /**
     * 设置某一个成员变量的值，注意，该变量必须是在desClass类所定义的变量。
     * <br></br>如果不知道变量定义在类继承层次结构哪一个类，请使用[.setField]
     *
     * @param desObj    需要设置的对象
     * @param desClass  目标变量所定义的类
     * @param fieldName 变量名称
     * @param value     需要设置的值
     * @return 是否设置成功
     */
    fun setField(desObj: Any?, desClass: Class<*>?, fieldName: String?, value: Any?): Boolean {
        require(!(desObj == null || desClass == null || fieldName == null)) { "parameter can not be null!" }
        try {
            // 注:使用desClass.getField(fieldName)这个方法只能获取到public的field
            val field = desClass.getDeclaredField(fieldName)
            field.isAccessible = true
            field[desObj] = value
            return true
        } catch (ignore: Exception) {
            //e.printStackTrace();
        }
        return false
    }

    /**
     * 设置某一个成员变量的值。
     * <br></br>为了提高效率，如果知道变量定义在类继承层次结构哪一个类，请使用[.setField]
     *
     * @param desObj    需要设置的对象
     * @param fieldName 变量名称
     * @param value     需要设置的值
     * @return 是否设置成功
     */
    fun setField(desObj: Any?, fieldName: String?, value: Any): Boolean {
        require(!(desObj == null || fieldName == null)) { "parameter can not be null!" }
        val desClass: Class<*> = desObj.javaClass
        return setFieldStepwise(desObj, desClass, fieldName, value)
    }

    private fun setFieldStepwise(
        desObj: Any,
        rootClass: Class<*>,
        fieldName: String,
        value: Any
    ): Boolean {
        var desClass: Class<*>? = rootClass
        while (desClass != null) {
            desClass = if (setField(desObj, desClass, fieldName, value)) {
                return true
            } else {
                try {
                    desClass.superclass
                } catch (e: Exception) {
                    null
                }
            }
        }
        return false
    }
}