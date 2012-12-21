/* JNI wrapper stuff */

#include <jni.h>
#include <stdio.h>
#include <android/log.h>
#include <string.h>

extern JNIEnv *SDL_ANDROID_GetJNIEnv();

typedef void (*audio_callback_t)(char *buffer, int bufsize);
static audio_callback_t audio_callback = NULL;

JNIEXPORT void JNICALL
native_audio_callback(JNIEnv* env, jobject thiz, jbyteArray buf, jint bufsize)
{
	if ( audio_callback == NULL )
		return;

	jboolean iscopy;
    jbyte* bbuf = (*env)->GetByteArrayElements(env, buf, &iscopy);
	audio_callback((char *)bbuf, bufsize);
	(*env)->ReleaseByteArrayElements(env, buf, bbuf, 0);
}

static JNINativeMethod methods[] = {
	{ "nativeAudioCallback", "([BI)V", (void *)&native_audio_callback }
};

void audioin_init(audio_callback_t callback) {
	JNIEnv *env = SDL_ANDROID_GetJNIEnv();
	jclass cls = (*env)->FindClass(env, "org/renpy/android/AudioIn");
	(*env)->RegisterNatives(env, cls, methods, sizeof(methods) / sizeof(methods[0]));

	audio_callback = callback;
}

