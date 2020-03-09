package ru.quandastudio.lpsclient.core

import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type

class EnumConverterFactory : Converter.Factory() {
    override fun stringConverter(
        type: Type,
        annotations: Array<Annotation>,
        retrofit: Retrofit
    ): Converter<*, String>? =
        if (type is Class<*> && type.isEnum) {
            Converter { enum: Any -> enum.toString() }
        } else {
            null
        }
}