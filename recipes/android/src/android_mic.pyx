__all__ = ('mic_register_callback', 'mic_unregister_callback',
    'mic_start', 'mic_stop')

cdef extern from "android_mic.h":
    ctypedef void (* audio_callback_t)(char *, int)
    void audioin_init(audio_callback_t)

cdef extern from "Python.h":
    object PyString_FromStringAndSize(char *s, Py_ssize_t len)

py_audio_callback = None

def mic_register_callback(callback):
    global py_audio_callback
    py_audio_callback = callback

AudioIn = None

def _ensure_audioin():
    global AudioIn
    if AudioIn is None:
        from jnius import autoclass
        AudioIn = autoclass('org.renpy.android.AudioIn')

def mic_start():
    _ensure_audioin()
    AudioIn.start_recording()

def mic_stop():
    _ensure_audioin()
    AudioIn.stop_recording()


cdef void cy_audio_callback(char *buf, int bufsize) nogil:
    with gil:
        if py_audio_callback is None:
            return
        py_buf = PyString_FromStringAndSize(buf, bufsize)
        py_audio_callback(py_buf)

# register the cython audio callback
audioin_init(cy_audio_callback)
