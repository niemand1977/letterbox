SVGs  = apple-158419.svg          elephant-24722.svg         jaguar-animal-157733.svg     mouse-157152.svg             rainbow-307622.svg    table-furniture-158162.svg  whale-311420.svg
SVGs += ducky-bath-2022661.svg          fox-animal-2026297.svg     jellyfish-311097.svg         nest-311931.svg              rainbow-wikipedia.svg unicorn-3354612.svg         xylophone-32128.svg
SVGs += bunny-animal-1294929.svg  giraffe-311104.svg         kangaroo-animal-2024310.svg  octopus-cartoon-2027746.svg  snake-303696.svg      unicorn-4124551.svg         yak-150568.svg
SVGs += chameleon-30238.svg       igel-hedgehog-1295076.svg  lion-africa-1300564.svg      penguin-42936.svg            bear-5096264.svg  vulture-159478.svg          zebra-470305.svg

LAUNCHER_SVG = app/src/main/res/drawable/src/ic_launcher.svg


LETTERS_PNG = app/src/main/res/drawable/src/ic_letters.png

all: assets icons menu-icons

assets:
	$(foreach svg,$(SVGs),\
		rsvg-convert --keep-aspect-ratio --width=1000 --height=1000 app/src/main/res/drawable/src/$(svg) | convert - -trim +repage -resize 400x400\> app/src/main/res/drawable/$$(echo $(svg) | cut -f1 -d-).png; \
	)

icons: $(LAUNCHER_SVG)
	rsvg-convert --width=48  --height=48  -o app/src/main/res/mipmap-mdpi/ic_launcher.png       $(LAUNCHER_SVG)
	rsvg-convert --width=48  --height=48  -o app/src/main/res/mipmap-mdpi/ic_launcher_round.png  $(LAUNCHER_SVG)
	rsvg-convert --width=72  --height=72  -o app/src/main/res/mipmap-hdpi/ic_launcher.png        $(LAUNCHER_SVG)
	rsvg-convert --width=72  --height=72  -o app/src/main/res/mipmap-hdpi/ic_launcher_round.png  $(LAUNCHER_SVG)
	rsvg-convert --width=96  --height=96  -o app/src/main/res/mipmap-xhdpi/ic_launcher.png       $(LAUNCHER_SVG)
	rsvg-convert --width=96  --height=96  -o app/src/main/res/mipmap-xhdpi/ic_launcher_round.png $(LAUNCHER_SVG)
	rsvg-convert --width=144 --height=144 -o app/src/main/res/mipmap-xxhdpi/ic_launcher.png      $(LAUNCHER_SVG)
	rsvg-convert --width=144 --height=144 -o app/src/main/res/mipmap-xxhdpi/ic_launcher_round.png $(LAUNCHER_SVG)
	rsvg-convert --width=192 --height=192 -o app/src/main/res/mipmap-xxxhdpi/ic_launcher.png     $(LAUNCHER_SVG)
	rsvg-convert --width=192 --height=192 -o app/src/main/res/mipmap-xxxhdpi/ic_launcher_round.png $(LAUNCHER_SVG)
	rsvg-convert --width=512 --height=512 -o fastlane/metadata/android/en-US/images/icon.png     $(LAUNCHER_SVG)


# Popup menu icons are drawn at their intrinsic size, so they need one bitmap
# per density at 24dp rather than a single large drawable.
menu-icons: $(LETTERS_PNG)
	$(foreach d,mdpi:24 hdpi:36 xhdpi:48 xxhdpi:72 xxxhdpi:96,\
		mkdir -p app/src/main/res/drawable-$(word 1,$(subst :, ,$(d))); \
		convert $(LETTERS_PNG) -strip -resize $(word 2,$(subst :, ,$(d)))x$(word 2,$(subst :, ,$(d))) app/src/main/res/drawable-$(word 1,$(subst :, ,$(d)))/ic_letters.png; \
	)
