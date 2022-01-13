# -f --format = sample format, stereo , 16 bit, LE = little endian
# -c --channels = number of channels, 1 fuer mono
# -r --rate = sampling rate in Hertz 2000 bis 192000 Hz
# -d --duration = Dauer. A value of zero means infinity. The default is zero.

#arecord -f S16_LE -r 22050 | lame - test_mono_halb.mp3
#arecord -f S16_LE | lame - test_stereo2.mp3
#arecord -f U8 -d 0 | lame - test_mono.mp3

arecord -f S16_LE -c 1 -r 44100 | lame - audio.mp3

# Bearbeitung mit Audacity
# 1. Select > Edit > Delete
# 2. Amplify (Lautstärke)
