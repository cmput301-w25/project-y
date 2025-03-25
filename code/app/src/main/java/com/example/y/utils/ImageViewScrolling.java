package com.example.y.utils;

import android.animation.Animator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.y.R;
import com.example.y.views.SlotMachineActivity;

/**
 * Image scrolling view for the slot machine.
 * Source: https://www.youtube.com/watch?v=Ja2MEpWUyYE
 */
public class ImageViewScrolling extends FrameLayout {

    private ImageView currentImage;
    private ImageView nextImage;
    private int oldValue = 0;
    private SpinEventEndListener eventEnd;

    public interface SpinEventEndListener {
        void onSpinFinish(int result, int count);
    }

    public ImageViewScrolling(@NonNull Context context) {
        super(context);
        init(context);
    }

    public ImageViewScrolling(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.image_view_scrolling, this);
        currentImage = getRootView().findViewById(R.id.current_image);
        nextImage = getRootView().findViewById(R.id.next_image);

        nextImage.setTranslationY(getHeight());
    }

    public void setValueRandom(int imageIndex, int rotateCount) {
        int ANIMATION_DUR = 150;
        currentImage.animate().translationY(-getHeight()).setDuration(ANIMATION_DUR).start();
        nextImage.setTranslationY(nextImage.getHeight());
        nextImage.animate().translationY(0)
                .setDuration(ANIMATION_DUR)
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(@NonNull Animator animator) {}

                    @Override
                    public void onAnimationEnd(@NonNull Animator animator) {
                        setImage(currentImage, oldValue % SlotMachineActivity.IMG_COUNT);
                        currentImage.setTranslationY(0);
                        if (oldValue != rotateCount) {
                            setValueRandom(imageIndex, rotateCount);
                            oldValue++;
                        } else {
                            oldValue = 0;
                            setImage(nextImage, imageIndex);
                            eventEnd.onSpinFinish(imageIndex % SlotMachineActivity.IMG_COUNT, rotateCount);
                        }
                    }

                    @Override
                    public void onAnimationCancel(@NonNull Animator animator) {}

                    @Override
                    public void onAnimationRepeat(@NonNull Animator animator) {}
                });
    }

    private void setImage(ImageView imageView, int index) {
        imageView.setImageResource(SlotMachineSymbol.values()[index].getImage());
        imageView.setTag(index);
    }

    public int getValue() {
        return Integer.parseInt(nextImage.getTag().toString());
    }

    public void setEventEnd(SpinEventEndListener eventEnd) {
        this.eventEnd = eventEnd;
    }

}
