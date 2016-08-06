package com.vosaye.bunkr.externalpackages.master;

import java.util.ArrayList;

import com.vosaye.bunkr.R;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;



public class TitleFlowIndicator extends TextView implements FlowIndicator 
{
	private static final float TITLE_PADDING = 10.0f;
	private static final float CLIP_PADDING = 0.0f;
	public static final int SELECTED_COLOR = 0x00BFFF45;
	private static final boolean SELECTED_BOLD = false;
	private static final int TEXT_COLOR = 0xFFAAAAAA;
	private static final int TEXT_SIZE = 14;
	private static final float FOOTER_LINE_HEIGHT = 1.0f;
	private static final int FOOTER_COLOR = 0x00BFFF45;
	private static final float FOOTER_TRIANGLE_HEIGHT = 7;
	private ViewFlow_Master viewFlow;
	private int currentScroll = 0;
	private TitleProvider titleProvider = null;
	private int currentPosition = 0;
	private Paint paintText;
	private Paint paintSelected;
	private Path path;
	private Paint paintFooterLine;
	private Paint paintFooterTriangle;
	private float footerTriangleHeight;
	private float titlePadding;
	private float clipPadding;
	private float footerLineHeight;

	private static final int SANS = 1;
	private static final int SERIF = 2;
	private static final int MONOSPACE = 3;

	private Typeface typeface;

	public TitleFlowIndicator(Context context) 
	{
		super(context);
		initDraw(TEXT_COLOR, TEXT_SIZE, SELECTED_COLOR, SELECTED_BOLD, TEXT_SIZE, FOOTER_LINE_HEIGHT, FOOTER_COLOR);
	}

	@SuppressLint("Recycle")
	public TitleFlowIndicator(Context context, AttributeSet attrs) 
	{
		super(context, attrs);

		int typefaceIndex = attrs.getAttributeIntValue("http://schemas.android.com/apk/res/android", "typeface", 0);
		int textStyleIndex = attrs.getAttributeIntValue("http://schemas.android.com/apk/res/android", "textStyle", 0);
		TypedArray a = context.obtainStyledAttributes(attrs,R.styleable.TitleFlowIndicator);
		String customTypeface = a.getString(R.styleable.TitleFlowIndicator_customTypeface);
		
		int footerColor = a.getColor(R.styleable.TitleFlowIndicator_footerColor, FOOTER_COLOR);
		footerLineHeight = a.getDimension(R.styleable.TitleFlowIndicator_footerLineHeight, FOOTER_LINE_HEIGHT);
		footerTriangleHeight = a.getDimension(R.styleable.TitleFlowIndicator_footerTriangleHeight, FOOTER_TRIANGLE_HEIGHT);
		int selectedColor = a.getColor(R.styleable.TitleFlowIndicator_selectedColor, SELECTED_COLOR);
		boolean selectedBold = a.getBoolean(R.styleable.TitleFlowIndicator_selectedBold, SELECTED_BOLD);
		int textColor = a.getColor(R.styleable.TitleFlowIndicator_textColor, TEXT_COLOR);
		float textSize = a.getDimension(R.styleable.TitleFlowIndicator_textSize, TEXT_SIZE);
		float selectedSize = a.getDimension(R.styleable.TitleFlowIndicator_selectedSize, textSize);
		titlePadding = a.getDimension(R.styleable.TitleFlowIndicator_titlePadding, TITLE_PADDING);
		clipPadding = a.getDimension(R.styleable.TitleFlowIndicator_clipPadding, CLIP_PADDING);
		initDraw(textColor, textSize, selectedColor, selectedBold, selectedSize, footerLineHeight, footerColor);

		if (customTypeface != null)
			typeface = Typeface.createFromAsset(context.getAssets(), customTypeface);
		else
			typeface = getTypefaceByIndex(typefaceIndex);
		typeface = Typeface.create(typeface, textStyleIndex);
	}

	private void initDraw(int textColor, float textSize, int selectedColor, boolean selectedBold, float selectedSize, float footerLineHeight, int footerColor)
	{
		paintText = new Paint();
		paintText.setColor(Color.parseColor("#eeeeee"));
		paintText.setTextSize(textSize);
		paintText.setAntiAlias(true);
		paintSelected = new Paint();
		//paintSelected.setColor(selectedColor);
		paintSelected.setTextSize(selectedSize);
		paintSelected.setColor(Color.parseColor("#ffffff"));
		paintSelected.setFakeBoldText(selectedBold);
		paintSelected.setAntiAlias(true);
		paintFooterLine = new Paint();
		paintFooterLine.setStyle(Paint.Style.FILL_AND_STROKE);
		paintFooterLine.setStrokeWidth(footerLineHeight);
		paintFooterLine.setColor(Color.parseColor("#ffffff"));
		paintFooterTriangle = new Paint();
		paintFooterTriangle.setStyle(Paint.Style.FILL_AND_STROKE);
		paintFooterTriangle.setColor(Color.parseColor("#ffffff"));
	}

	@SuppressLint("DrawAllocation")
	@Override
	protected void onDraw(Canvas canvas) 
	{
		super.onDraw(canvas);
		ArrayList<Rect> bounds = calculateAllBounds(paintText);
		int count = (viewFlow != null && viewFlow.getAdapter() != null) ? viewFlow.getAdapter().getCount() : 1;
		Rect curViewBound = bounds.get(currentPosition);
		int curViewWidth = curViewBound.right - curViewBound.left;
		if (curViewBound.left < 0) 
		{
			clipViewOnTheLeft(curViewBound, curViewWidth);
		}
		
		if (curViewBound.right > getLeft() + getWidth()) 
		{
			clipViewOnTheRight(curViewBound, curViewWidth);
		}

		if (currentPosition > 0) 
		{
			for (int iLoop = currentPosition - 1; iLoop >= 0; iLoop--) 
			{
				Rect bound = bounds.get(iLoop);
				int w = bound.right - bound.left;
			
				if (bound.left < 0) 
				{
					clipViewOnTheLeft(bound, w);
					if (iLoop < count - 1 && currentPosition != iLoop) 
					{
						Rect rightBound = bounds.get(iLoop + 1);
					
						if (bound.right + TITLE_PADDING > rightBound.left) 
						{
							bound.left = rightBound.left - (w + (int) titlePadding);
						}
					}
				}
			}
		}
		
		if (currentPosition < count - 1) 
		{
			for (int iLoop = currentPosition + 1; iLoop < count; iLoop++) 
			{
				Rect bound = bounds.get(iLoop);
				int w = bound.right - bound.left;
			
				if (bound.right > getLeft() + getWidth()) 
				{
					clipViewOnTheRight(bound, w);
				
					if (iLoop > 0 && currentPosition != iLoop) 
					{
						Rect leftBound = bounds.get(iLoop - 1);
					
						if (bound.left - TITLE_PADDING < leftBound.right) 
						{
							bound.left = leftBound.right + (int) titlePadding;
						}
					}
				}
			}
		}

		for (int iLoop = 0; iLoop < count; iLoop++) 
		{
			String title = getTitle(iLoop);
			Rect bound = bounds.get(iLoop);
		
			if ((bound.left > getLeft() && bound.left < getLeft() + getWidth()) || (bound.right > getLeft() && bound.right < getLeft() + getWidth())) 
			{
				Paint paint = paintText;
			
				int middle = (bound.left + bound.right) / 2;
				if (Math.abs(middle - (getWidth() / 2)) < 20) 
				{
					paint = paintSelected;
				}
				paint.setTypeface(typeface);
				canvas.drawText(title, bound.left, bound.bottom, paint);
			}
		}

		path = new Path();
		int coordY = getHeight();
		coordY -=  footerLineHeight/2;
		path.moveTo(0, coordY);
		path.lineTo(getWidth(), coordY);
		path.close();
		canvas.drawPath(path, paintFooterLine);

		path = new Path();
		path.moveTo(getWidth() / 2, getHeight() - footerLineHeight - footerTriangleHeight);
		path.lineTo(getWidth() / 2 + footerTriangleHeight, getHeight() - footerLineHeight);
		path.lineTo(getWidth() / 2 - footerTriangleHeight, getHeight() - footerLineHeight);
		path.close();
		canvas.drawPath(path, paintFooterTriangle);
	}

	private void clipViewOnTheRight(Rect curViewBound, int curViewWidth) 
	{
		curViewBound.right = getLeft() + getWidth() - (int) clipPadding;
		curViewBound.left = curViewBound.right - curViewWidth;
	}

	private void clipViewOnTheLeft(Rect curViewBound, int curViewWidth) 
	{
		curViewBound.left = 0 + (int) clipPadding;
		curViewBound.right = curViewWidth;
	}

	private ArrayList<Rect> calculateAllBounds(Paint paint) 
	{
		ArrayList<Rect> list = new ArrayList<Rect>();
	
		int count = (viewFlow != null && viewFlow.getAdapter() != null) ? viewFlow.getAdapter().getCount() : 1;
		for (int iLoop = 0; iLoop < count; iLoop++) 
		{
			Rect bounds = calcBounds(iLoop, paint);
			int w = (bounds.right - bounds.left);
			int h = (bounds.bottom - bounds.top);
			bounds.left = (getWidth() / 2) - (w / 2) - currentScroll + (iLoop * getWidth());
			bounds.right = bounds.left + w;
			bounds.top = 0;
			bounds.bottom = h;
			list.add(bounds);
		}
		return list;
	}

	private Rect calcBounds(int index, Paint paint) 
	{
		String title = getTitle(index);	
		Rect bounds = new Rect();
		bounds.right = (int) paint.measureText(title);
		bounds.bottom = (int) (paint.descent() - paint.ascent());
		return bounds;
	}

	private String getTitle(int pos) 
	{
		String title = "title " + pos;
		if (titleProvider != null) 
		{
			title = titleProvider.getTitle(pos);
		}
		return title;
	}

	@Override
	public void onScrolled(int h, int v, int oldh, int oldv)
	{
		currentScroll = h;
		invalidate();
	}

	@Override
	public void onSwitched(View view, int position) 
	{
		currentPosition = position;
		invalidate();
	}

	@Override
	public void setViewFlow(ViewFlow_Master view) 
	{
		viewFlow = view;
		currentPosition = view.getSelectedItemPosition();
		invalidate();
	}

	public void setTitleProvider(TitleProvider provider) 
	{
		titleProvider = provider;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) 
	{
		setMeasuredDimension(measureWidth(widthMeasureSpec), measureHeight(heightMeasureSpec));
	}

	private int measureWidth(int measureSpec) 
	{
		int result = 0;
		int specMode = MeasureSpec.getMode(measureSpec);
		int specSize = MeasureSpec.getSize(measureSpec);

		if (specMode != MeasureSpec.EXACTLY) 
		{
			throw new IllegalStateException("ViewFlow can only be used in EXACTLY mode.");
		}
		result = specSize;
		return result;
	}

	private int measureHeight(int measureSpec) 
	{
		int result = 0;
		int specMode = MeasureSpec.getMode(measureSpec);
		int specSize = MeasureSpec.getSize(measureSpec);

		if (specMode == MeasureSpec.EXACTLY) 
		{
			result = specSize;
		}
		else 
		{
			Rect bounds = new Rect();
			bounds.bottom = (int) (paintText.descent() - paintText.ascent());
			result = bounds.bottom - bounds.top + (int) footerTriangleHeight + (int) footerLineHeight + 10;
			return result;
		}
		return result;
	}

	private Typeface getTypefaceByIndex(int typefaceIndex) 
	{
		switch (typefaceIndex) 
		{
		case SANS:
			return Typeface.SANS_SERIF;

		case SERIF:
			return Typeface.SERIF;

		case MONOSPACE:
			return Typeface.MONOSPACE;
		default:
			return Typeface.DEFAULT;
		}
	}
}