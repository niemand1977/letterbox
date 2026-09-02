SVGs  = apple-158419.svg          elephant-24722.svg         jaguar-animal-157733.svg     mouse-157152.svg             rainbow-307622.svg    table-furniture-158162.svg  whale-311420.svg
SVGs += ducky-bath-2022661.svg          fox-animal-2026297.svg     jellyfish-311097.svg         nest-311931.svg              rainbow-wikipedia.svg unicorn-3354612.svg         xylophone-32128.svg
SVGs += bunny-animal-1294929.svg  giraffe-311104.svg         kangaroo-animal-2024310.svg  octopus-cartoon-2027746.svg  snake-303696.svg      unicorn-4124551.svg         yak-150568.svg
SVGs += chameleon-30238.svg       igel-hedgehog-1295076.svg  lion-africa-1300564.svg      penguin-42936.svg            bear-5096264.svg  vulture-159478.svg          zebra-470305.svg

LAUNCHER_SVG = app/src/main/res/drawable/src/ic_launcher.svg


all: assets icons

assets:
	$(foreach svg,$(SVGs),\
		rsvg-convert --keep-aspect-ratio --width=400 --height=400  --output app/src/main/res/drawable/$$(echo $(svg) | cut -f1 -d-).png  app/src/main/res/drawable/src/$(svg); \
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

