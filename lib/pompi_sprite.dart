import 'package:flutter/material.dart';

/// Displays one cell of the same atlas used by Android's native overlay.
class PompiSprite extends StatelessWidget {
  const PompiSprite({super.key, this.frame = 0, this.size = 180});
  final int frame;
  final double size;

  @override
  Widget build(BuildContext context) => SizedBox(
    width: size,
    height: size,
    child: ClipRect(
      child: OverflowBox(
        alignment: Alignment.topLeft,
        maxWidth: size * 2,
        maxHeight: size * 2,
        child: Transform.translate(
          offset: Offset(-(frame % 2) * size, -(frame ~/ 2) * size),
          child: Image.asset(
            'assets/pompi-atlas.png',
            width: size * 2,
            height: size * 2,
          ),
        ),
      ),
    ),
  );
}
